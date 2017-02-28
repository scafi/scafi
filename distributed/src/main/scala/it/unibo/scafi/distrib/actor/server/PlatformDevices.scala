package it.unibo.scafi.distrib.actor.server

import akka.actor.{Actor, Props, ActorRef}
import scala.concurrent.duration.DurationInt

/**
 * @author Roberto Casadei
 *
 */

trait PlatformDevices { self: Platform.Subcomponent =>

  /**
   * Neighbourhood management for devices in a server-based platform.
   */
  trait DeviceNbrManagementBehavior extends BaseDeviceActor with BaseNbrManagementBehavior { selfActor: Actor =>
    override def inputManagementBehavior = super.inputManagementBehavior.orElse {
      // Neighborhood management
      case MsgNeighborhood(this.selfId, nbs) => updateNeighborhood(nbs, clear = true)
      case MsgExports(exports) => updateNeighborsState(exports.mapValues(Some(_)), clear = true)
      case MsgNeighborhoodExports(this.selfId, exps) => updateNeighborsState(exps, clear = true)
    }
  }

  /**
   * Specializes a [[ComputationDeviceActor]] to work with a central
   *  "server" {{server}} (aka [[ServerActor]]).
   * Notes
   *   - The device registers itself to the {{server}} on start
   *   - With a fixed {{NEIGHBORHOOD_LOOKUP_INTERVAL}}, the device asks
   *     the {{server}} for the state of its neighbors
   *   - The state of the device itself ({{PropagateExportToNeighbors}})
   *     is sent to the server {{server}}
   */
  class DeviceActor(override val selfId: ID,
                    override var aggregateExecutor: Option[ExecutionTemplate],
                    override var execScope: ExecScope,
                    val server: ActorRef)
    extends DynamicComputationDeviceActor
    with MissingCodeManagementBehavior
    with ObservableDeviceActor
    with QueryableDeviceActorBehavior
    with DeviceNbrManagementBehavior {
    val NEIGHBORHOOD_LOOKUP_INTERVAL = 2.seconds

    override def AfterJob() = {
      super.AfterJob()
      lastExport.foreach(server ! MsgExport(selfId, _))
    }

    override def preStart() = {
      super.preStart()
      import context.dispatcher
      context.system.scheduler.schedule(NEIGHBORHOOD_LOOKUP_INTERVAL,
        NEIGHBORHOOD_LOOKUP_INTERVAL,
        server,
        MsgGetNeighborhoodExports(selfId))
      server ! MsgRegistration(selfId)
    }

    override def updateSensorValues() = {
      super.updateSensorValues()
      sensorValues.foreach(sns => notifySensorValueToServer(sns._1, sns._2))
    }

    override def setLocalSensorValue(name: LSNS, value: Any): Unit = {
      super.setLocalSensorValue(name, value)
      notifySensorValueToServer(name, value)
    }

    def notifySensorValueToServer(name: LSNS, value: Any) = {
      server ! MsgSensorValue(selfId, name, value)
      logger.debug(s"\nSENSOR ${name} => ${value}")
    }

    override def PropagateExportToNeighbors(export: EXPORT) =
      server ! MsgExport(selfId, export)
  }

  object DeviceActor extends Serializable {
    def props(selfId: ID,
              program: Option[ExecutionTemplate],
              execStrategy: ExecScope,
              serverActor: ActorRef) =
      Props(classOf[DeviceActor], self, selfId, program, execStrategy, serverActor)
  }
}
