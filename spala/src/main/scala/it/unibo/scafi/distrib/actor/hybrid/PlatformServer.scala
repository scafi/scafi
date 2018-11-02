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

package it.unibo.scafi.distrib.actor.hybrid

import akka.actor.{Actor, ActorRef, Props}
import it.unibo.scafi.distrib.actor.{GoOn, MsgStart}
import it.unibo.scafi.distrib.actor.patterns.{BasicActorBehavior, ObservableActorBehavior}

import scala.collection.mutable.{Map => MMap}

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

    def neighborhood(id: UID): Set[UID]

    // CONCRETE MEMBERS

    val map: MMap[UID, ActorRef] = MMap()

    def lookupActor(id: UID): Option[ActorRef] = map.get(id)

    def registerDevice(devId: UID, ref: ActorRef): Unit = {
      map += (devId -> sender)
    }

    // REACTIVE BEHAVIOR

    override def receive: Receive = super.receive.orElse(setupBehavior)

    override def queryManagementBehavior: Receive = {
      case MsgGetNeighborhood(devId) => sender ! MsgNeighborhood(devId, neighborhood(devId))
      case MsgLookup(id) => lookupActor(id).foreach(ref => sender ! MsgDeviceLocation(id, ref))
    }

    def setupBehavior: Receive = {
      case MsgRegistration(devId) =>
        logger.info(s"\nDevice $devId has registered itself (ref: $sender)")
        registerDevice(devId, sender)
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
  }

  class ServerActor()
    extends AbstractServerActor
    with ObservableServerActor
    with MissingCodeManagementBehavior {

    val neighborhoods: MMap[UID, Set[UID]] = MMap()

    def neighborhood(id: UID): Set[UID] = neighborhoods.getOrElse(id, Set())

    override def inputManagementBehavior: Receive = super.inputManagementBehavior orElse {
      case MsgNeighbor(id, idn) => addNbrsTo(id, Set(idn))
      case MsgNeighborhood(id, nbrs) => addNbrsTo(id, nbrs)
    }

    def addNbrsTo(id: UID, nbrs: Set[UID]): Unit = {
      neighborhoods += id -> (neighborhood(id) ++ nbrs)
      notifyObservers(MsgNeighborhood(id,nbrs))
    }
  }

  object ServerActor extends Serializable {
    def props(): Props = Props(classOf[ServerActor], self)
  }
}

