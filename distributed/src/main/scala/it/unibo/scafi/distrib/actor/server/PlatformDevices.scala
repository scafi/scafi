/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.distrib.actor.server

import akka.actor.{Actor, Props, ActorRef}
import scala.concurrent.duration.DurationInt

trait PlatformDevices { self: Platform.Subcomponent =>

  /**
   * Neighbourhood management for devices in a server-based platform.
   */
  trait DeviceNbrManagementBehavior extends BaseDeviceActor with BaseNbrManagementBehavior { selfActor: Actor =>
    override def inputManagementBehavior: Receive = super.inputManagementBehavior.orElse {
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

    override def afterJob(): Unit = {
      super.afterJob()
      lastExport.foreach(server ! MsgExport(selfId, _))
    }

    override def preStart(): Unit = {
      super.preStart()
      import context.dispatcher
      context.system.scheduler.schedule(NEIGHBORHOOD_LOOKUP_INTERVAL,
        NEIGHBORHOOD_LOOKUP_INTERVAL,
        server,
        MsgGetNeighborhoodExports(selfId))
      server ! MsgRegistration(selfId)
    }

    override def updateSensorValues(): Unit = {
      super.updateSensorValues()
      sensorValues.foreach(sns => notifySensorValueToServer(sns._1, sns._2))
    }

    override def setLocalSensorValue(name: LSNS, value: Any): Unit = {
      super.setLocalSensorValue(name, value)
      notifySensorValueToServer(name, value)
    }

    def notifySensorValueToServer(name: LSNS, value: Any): Unit = {
      server ! MsgSensorValue(selfId, name, value)
      logger.debug(s"\nSENSOR ${name} => ${value}")
    }

    override def propagateExportToNeighbors(export: EXPORT): Unit =
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
