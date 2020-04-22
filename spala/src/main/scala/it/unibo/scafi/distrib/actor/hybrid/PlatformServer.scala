/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.hybrid

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.distrib.actor.patterns.ObservableActorBehavior
import it.unibo.scafi.distrib.actor.server.PlatformBehaviors

import scala.collection.mutable.{Map => MMap}

trait PlatformServer extends PlatformBehaviors { self: HybridPlatform.Subcomponent =>
  trait ObservableServerActor extends ServerBaseServerActor with ObservableActorBehavior {
    override def receive: Receive = super.receive.orElse(observersManagementBehavior)

    override def registerDevice(id: UID, ref: ActorRef): Unit = {
      super.registerDevice(id, ref)
      notifyObservers(DevInfo(id, ref))
    }
  }

  class HybridServerActor()
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

  object HybridServerActor extends Serializable {
    def props(): Props = Props(classOf[HybridServerActor], self)
  }
}

