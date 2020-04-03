/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.server

import it.unibo.scafi.distrib.actor.{Platform => ActorPlatform}
import akka.actor.{Actor, ActorRef}
import it.unibo.scafi.distrib.actor.patterns.BasicActorBehavior

import scala.collection.mutable.{Map => MMap}

trait PlatformBehaviors { self: ActorPlatform =>
  /**
    * Neighbourhood management for devices in a server-based platform.
    */
  trait DeviceNbrManagementBehavior extends BaseDeviceActor with BaseNbrManagementBehavior { selfActor: Actor =>
    override def inputManagementBehavior: Receive = super.inputManagementBehavior.orElse {
      // Neighborhood management
      case MsgNeighborhood(this.selfId, nbs) => updateNeighborhood(nbs, clear = true)
      case MsgExports(exports) => updateNeighborsState(exports.mapValues(Some(_)).toMap, clear = true)
      case MsgNeighborhoodExports(this.selfId, exps) => updateNeighborsState(exps, clear = true)
    }
  }

  trait ServerBaseDeviceActor
    extends DynamicComputationDeviceActor
    with QueryableDeviceActorBehavior
    with DeviceNbrManagementBehavior {

    val server: ActorRef

    override def preStart(): Unit = {
      super.preStart()
      server ! MsgRegistration(selfId)
    }

    override def propagateMsgToNeighbors(msg: Any): Unit = {
      server ! msg
    }
  }

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
  trait ServerBaseServerActor extends Actor with BasicActorBehavior {
    // ABSTRACT MEMBERS
    def neighborhood(id: UID): Set[UID]

    // CONCRETE MEMBERS
    val map: MMap[UID, ActorRef] = MMap()
    def lookupActor(id: UID): Option[ActorRef] = map.get(id)
    def registerDevice(devId: UID, ref: ActorRef): Unit = {
      map += (devId -> sender)
    }

    // REACTIVE BEHAVIOR
    override def queryManagementBehavior: Receive = super.queryManagementBehavior orElse {
      case MsgGetNeighborhood(devId) => sender ! MsgNeighborhood(devId, neighborhood(devId))
      case MsgLookup(id) => lookupActor(id).foreach(ref => sender ! MsgDeviceLocation(id, ref))
    }
    override def receive: Receive = super.receive.orElse {
      case MsgRegistration(devId) =>
        logger.info(s"\nDevice $devId has registered itself (ref: $sender)")
        registerDevice(devId, sender)
    }
  }
}
