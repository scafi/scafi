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

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.distrib.actor.patterns.ObservableActorBehavior
import it.unibo.scafi.distrib.actor.server.PlatformBehaviors

import scala.collection.mutable.{Map => MMap}

trait PlatformServer extends PlatformBehaviors { self: Platform.Subcomponent =>
  trait ObservableServerActor extends ServerBaseServerActor with ObservableActorBehavior {
    override def receive: Receive = super.receive.orElse(observersManagementBehavior)

    override def registerDevice(id: UID, ref: ActorRef): Unit = {
      super.registerDevice(id, ref)
      notifyObservers(DevInfo(id, ref))
    }
  }

  class ServerActor()
    extends ServerBaseServerActor
    with ObservableServerActor
    with MissingCodeManagementBehavior {

    val neighborhoods: MMap[UID, Set[UID]] = MMap()

    override def neighborhood(id: UID): Set[UID] = neighborhoods.getOrElse(id, Set())

    override def queryManagementBehavior: Receive = super.queryManagementBehavior.orElse {
      case MsgGetNeighborhoodLocations(id) =>
        val locs = neighborhood(id)
          .filter(idn => lookupActor(idn).isDefined)
          .map(idn => idn -> lookupActor(idn).get.path.toString)
          .toMap
        sender ! MsgNeighborhoodLocations(id, locs)
    }

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

