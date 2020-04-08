/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.server

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.distrib.actor.{GoOn, MsgStart}
import it.unibo.scafi.distrib.actor.patterns.ObservableActorBehavior

import scala.collection.mutable.{Map => MMap}

trait PlatformServer extends PlatformBehaviors { self: ServerPlatform.Subcomponent =>

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
  trait AbstractServerActor extends ServerBaseServerActor {
    // ABSTRACT MEMBERS
    val scheduler: Option[ActorRef]

    // CONCRETE MEMBERS
    val exports = MMap[UID,ComputationExport]()
    val snsValues = MMap[UID,Map[LSensorName,Any]]()

    def start(): Unit = scheduler.foreach(_ ! GoOn)

    def exportsFor(id: UID): Map[UID, Option[ComputationExport]] =
      neighborhood(id).map(nbr => nbr -> exports.get(nbr)).toMap

    def addExports(exps: Map[UID, ComputationExport]): Unit = {
      exports ++= exps
    }

    def handleProgram(id: UID, program: () => Any): Unit =
      neighborhood(id).foreach(nbr => map(nbr) ! MsgUpdateProgram(id, program))

    override def registerDevice(devId: UID, ref: ActorRef): Unit = {
      map += (devId -> sender)
      scheduler.foreach(_ ! MsgWithDevices(Map(devId -> sender)))
    }

    def setSensorValue(id: UID, name: LSensorName, value: Any): Unit = {
      //logger.debug(s"\n${id}'s update for ${name} (=$value)")
      snsValues += (id -> (snsValues.getOrElse(id,Map()) + (name -> value)))
    }

    // REACTIVE BEHAVIOR
    override def queryManagementBehavior: Receive = super.queryManagementBehavior orElse {
      case MsgGetNeighborhoodExports(id) =>
        sender ! MsgNeighborhoodExports(id, exportsFor(id))
      //case MsgGetIds => sender ! map.keySet
    }

    override def inputManagementBehavior: Receive = super.inputManagementBehavior orElse {
      case MsgExport(id,export) => {
        //logger.debug(s"\nGot from id $id export $export")
        addExports(Map(id -> export))
      }
      case MsgUpdateProgram(id, program) => {
        handleProgram(id, program)
      }
      case MsgExports(exps) => {
        addExports(exps)
      }
      case MsgSensorValue(id, name, value) => {
        setSensorValue(id, name, value)
      }
    }

    override def commandManagementBehavior: Receive = super.commandManagementBehavior.orElse {
      case MsgStart => start()
    }
  }

  trait ObservableServerActor
    extends AbstractServerActor
    with ObservableActorBehavior {

    override def receive: Receive = super.receive
      .orElse(observersManagementBehavior)

    override def registerDevice(id: UID, ref: ActorRef): Unit = {
      super.registerDevice(id, ref)
      notifyObservers(DevInfo(id, ref))
    }

    override def addExports(exps: Map[UID, ComputationExport]): Unit = {
      super.addExports(exps)
      notifyObservers(MsgExports(exps))
    }

    override def handleProgram(id: UID, program: () => Any): Unit = {
      super.handleProgram(id, program)
      notifyObservers(MsgUpdateProgram(id, program))
    }

    override def setSensorValue(id: UID, name: LSensorName, value: Any): Unit = {
      super.setSensorValue(id, name, value)
      notifyObservers(MsgSensorValue(id, name, value))
    }
  }

  class ServerActor(val scheduler: Option[ActorRef])
    extends AbstractServerActor
    with ObservableServerActor
    with MissingCodeManagementBehavior {

    val neighborhoods = MMap[UID,Set[UID]]()

    def neighborhood(id: UID): Set[UID] = neighborhoods.getOrElse(id, Set())

    override def inputManagementBehavior: Receive = super.inputManagementBehavior orElse {
      case MsgNeighbor(id, idn) => {
        addNbrsTo(id, Set(idn))
      }
      case MsgNeighborhood(id, nbrs) => {
        addNbrsTo(id, nbrs)
      }
    }

    def addNbrsTo(id: UID, nbrs: Set[UID]): Unit = {
      neighborhoods += id -> (neighborhood(id) ++ nbrs)
      notifyObservers(MsgNeighborhood(id,nbrs))
    }
  }

  object ServerActor {
    def props(sched: Option[ActorRef] = None): Props =
      Props(classOf[ServerActor], self, sched)
  }
}

/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

/*package it.unibo.scafi.distrib.actor.server

import akka.actor.{Props, ActorRef, Actor}
import it.unibo.scafi.distrib.actor.{GoOn, MsgStart}
import it.unibo.scafi.distrib.actor.patterns.{ObservableActorBehavior, BasicActorBehavior}
import scala.collection.mutable.{ Map => MMap }

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
    def neighborhood(id: UID): Set[UID]

    // CONCRETE MEMBERS

    val map = MMap[UID,ActorRef]()
    val exports = MMap[UID,ComputationExport]()
    val snsValues = MMap[UID,Map[LSensorName,Any]]()

    def start(): Unit = scheduler.foreach(_ ! GoOn)

    def lookupActor(id: UID): Option[ActorRef] = map.get(id)

    def exportsFor(id: UID): Map[UID, Option[ComputationExport]] =
      neighborhood(id).map(nbr => nbr -> exports.get(nbr)).toMap

    def addExports(exps: Map[UID, ComputationExport]): Unit = {
      exports ++= exps
    }

    def handleProgram(id: UID, program: () => Any): Unit =
      neighborhood(id).foreach(nbr => map(nbr) ! MsgUpdateProgram(id, program))

    def registerDevice(devId: UID, ref: ActorRef): Unit = {
      map += (devId -> sender)
      scheduler.foreach(_ ! MsgWithDevices(Map(devId -> sender)))
    }

    def setSensorValue(id: UID, name: LSensorName, value: Any): Unit = {
      //logger.debug(s"\n${id}'s update for ${name} (=$value)")
      snsValues += (id -> (snsValues.getOrElse(id,Map()) + (name -> value)))
    }

    // REACTIVE BEHAVIOR

    override def receive: Receive = super.receive
      .orElse(setupBehavior)

    override def queryManagementBehavior: Receive = {
      case MsgGetNeighborhood(devId) => sender ! MsgNeighborhood(devId, neighborhood(devId))
      case MsgLookup(id) => {
        //logger.debug(s"$sender asked lookup for ID=$id")
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
      case MsgUpdateProgram(id, program) => {
        handleProgram(id, program)
      }
      case MsgExports(exps) => {
        addExports(exps)
      }
      case MsgSensorValue(id, name, value) => {
        setSensorValue(id, name, value)
      }
    }

    override def commandManagementBehavior: Receive = super.commandManagementBehavior.orElse {
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

    override def receive: Receive = super.receive
      .orElse(observersManagementBehavior)

    override def registerDevice(id: UID, ref: ActorRef): Unit = {
      super.registerDevice(id, ref)
      notifyObservers(DevInfo(id, ref))
    }

    override def addExports(exps: Map[UID, ComputationExport]): Unit = {
      super.addExports(exps)
      notifyObservers(MsgExports(exps))
    }

    override def handleProgram(id: UID, program: () => Any): Unit = {
      super.handleProgram(id, program)
      notifyObservers(MsgUpdateProgram(id, program))
    }

    override def setSensorValue(id: UID, name: LSensorName, value: Any): Unit = {
      super.setSensorValue(id, name, value)
      notifyObservers(MsgSensorValue(id, name, value))
    }
  }

  class ServerActor(val scheduler: Option[ActorRef])
    extends AbstractServerActor
      with ObservableServerActor
      with MissingCodeManagementBehavior {

    val neighborhoods = MMap[UID,Set[UID]]()

    def neighborhood(id: UID): Set[UID] = neighborhoods.getOrElse(id, Set())

    override def inputManagementBehavior: Receive = super.inputManagementBehavior orElse {
      case MsgNeighbor(id, idn) => {
        addNbrsTo(id, Set(idn))
      }
      case MsgNeighborhood(id, nbrs) => {
        addNbrsTo(id, nbrs)
      }
    }

    def addNbrsTo(id: UID, nbrs: Set[UID]): Unit = {
      neighborhoods += id -> (neighborhood(id) ++ nbrs)
      notifyObservers(MsgNeighborhood(id,nbrs))
    }
  }

  object ServerActor extends Serializable {
    def props(sched: Option[ActorRef] = None): Props =
      Props(classOf[ServerActor], self, sched)
  }
}*/