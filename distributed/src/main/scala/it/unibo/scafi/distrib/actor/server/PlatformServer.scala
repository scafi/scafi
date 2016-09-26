package it.unibo.scafi.distrib.actor.server

import akka.actor.{Props, ActorRef, Actor}
import it.unibo.scafi.distrib.actor.{GoOn, MsgStart}
import it.unibo.scafi.distrib.actor.patterns.{ObservableActorBehavior, BasicActorBehavior}
import scala.collection.mutable.{ Map => MMap }

/**
 * @author Roberto Casadei
 *
 */

trait PlatformServer { self: Platform.Subcomponent =>

  /**
   * This actor represents the singleton, central server of a
   *  distributed aggregate system of devices.
   * Responsibilities
   *   - Handles request ([[MsgRegistration]]) for entering the system
   *   - Keeps track of the neighborhoods
   *   - Receives and propagates the states of the devices
   *   - Provides a white-pages service: looks up the location of a given device ID
   *   - Represents an access point for information about the network
   */
  trait AbstractServerActor extends Actor with BasicActorBehavior {
    // ABSTRACT MEMBERS

    val scheduler: Option[ActorRef]
    def neighborhood(id: ID): Set[ID]

    // CONCRETE MEMBERS

    val map = MMap[ID,ActorRef]()
    val exports = MMap[ID,EXPORT]()
    val snsValues = MMap[ID,Map[LSNS,Any]]()

    def start(): Unit = scheduler.foreach(_ ! GoOn)

    def lookupActor(id: ID): Option[ActorRef] = map.get(id)

    def exportsFor(id: ID): Map[ID, Option[EXPORT]] =
      neighborhood(id).map(nbr => nbr -> exports.get(nbr)).toMap

    def addExports(exps: Map[ID, EXPORT]): Unit = {
      exports ++= exps
    }

    def registerDevice(devId: ID, ref: ActorRef): Unit = {
      map += (devId -> sender)
      scheduler.foreach(_ ! MsgWithDevices(Map(devId -> sender)))
    }

    def setSensorValue(id: ID, name: LSNS, value: Any): Unit = {
      //logger.debug(s"\n${id}'s update for ${name} (=$value)")
      snsValues += (id -> (snsValues.getOrElse(id,Map()) + (name -> value)))
    }

    // REACTIVE BEHAVIOR

    override def receive = super.receive
      .orElse(setupBehavior)

    override def queryManagementBehavior: Receive = {
      case MsgGetNeighborhood(devId) => sender ! MsgNeighborhood(devId, neighborhood(devId))
      case MsgLookup(id) => {
        //println(s"$sender asked lookup for ID=$id")
        lookupActor(id).foreach(ref => sender ! MsgDeviceLocation(id, ref))
      }
      case MsgGetNeighborhoodExports(id) => {
        sender ! MsgNeighborhoodExports(id, exportsFor(id))
      }
      //case MsgGetIds => sender ! map.keySet
    }

    override def inputManagementBehavior: Receive = super.inputManagementBehavior orElse {
      case MsgExport(id,export) => {
        //logger.debug(s"\nGot from id $id export $export")
        addExports(Map(id -> export))
      }
      case MsgExports(exps) => {
        addExports(exps)
      }
      case MsgSensorValue(id, name, value) => {
        setSensorValue(id, name, value)
      }
    }

    override def commandManagementBehavior = super.commandManagementBehavior.orElse {
      case MsgStart => start()
    }

    def setupBehavior: Receive = {
      case MsgRegistration(devId) => {
        logger.info(s"\nDevice $devId has registered itself (ref: $sender)")
        registerDevice(devId, sender)
      }
    }
  }

  trait ObservableServerActor extends AbstractServerActor
  with ObservableActorBehavior {

    override def receive = super.receive
      .orElse(observersManagementBehavior)

    override def registerDevice(id: ID, ref: ActorRef) = {
      super.registerDevice(id, ref)
      NotifyObservers(DevInfo(id, ref))
    }

    override def addExports(exps: Map[ID, EXPORT]): Unit = {
      super.addExports(exps)
      NotifyObservers(MsgExports(exps))
    }

    override def setSensorValue(id: ID, name: LSNS, value: Any): Unit = {
      super.setSensorValue(id, name, value)
      NotifyObservers(MsgSensorValue(id, name, value))
    }
  }

  class ServerActor(val scheduler: Option[ActorRef])
    extends AbstractServerActor
    with ObservableServerActor
    with MissingCodeManagementBehavior {

    val neighborhoods = MMap[ID,Set[ID]]()

    def neighborhood(id: ID): Set[ID] = neighborhoods.getOrElse(id, Set())

    override def inputManagementBehavior: Receive = super.inputManagementBehavior orElse {
      case MsgNeighbor(id, idn) => {
        addNbrsTo(id, Set(idn))
      }
      case MsgNeighborhood(id, nbrs) => {
        addNbrsTo(id, nbrs)
      }
    }

    def addNbrsTo(id: ID, nbrs: Set[ID]): Unit = {
      neighborhoods += id -> (neighborhood(id) ++ nbrs)
      NotifyObservers(MsgNeighborhood(id,nbrs))
    }
  }

  object ServerActor extends Serializable {
    def props(sched: Option[ActorRef] = None) =
      Props(classOf[ServerActor], self, sched)
  }
}
