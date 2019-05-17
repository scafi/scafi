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

package it.unibo.scafi.distrib.actor.p2p

import it.unibo.scafi.distrib.actor.{Platform => ActorPlatform}
import akka.actor.{Actor, ActorRef}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

trait PlatformBehaviors { self: ActorPlatform =>
  /**
    * Neighbourhood management for devices in a P2P platform.
    */
  trait P2pNbrManagementBehavior extends BaseNbrManagementBehavior { selfActor: Actor =>
    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(neighbourhoodManagementBehavior)

    def neighbourhoodManagementBehavior: Receive = {
      case info @ NbrInfo(idn,_,_,_) => mergeNeighborInfo(idn,info)
      case MsgDeviceLocation(idn, ref) => mergeNeighborInfo(idn,NbrInfo(idn,None,Some(ref),None))
      case MsgNeighborhoodUpdate(_, nbs) => replaceNeighborhood(nbs.map(n => n._1 -> (Some(n._2), None)))
      case MsgNeighborhoodLocations(_, nbs) => replaceNeighborhood(nbs.map(n => n._1 -> (None, Some(n._2))))
      case MsgExport(from, export) =>
        mergeNeighborInfo(from, NbrInfo(from, None, Some(sender), Some(sender.path.toString)))
        updateNeighborsState(Map(from -> Some(export)))
      case MsgRemoveNeighbor(idn) => removeNeighbor(idn)
    }

    def replaceNeighborhood(neighbors: Map[UID, (Option[ActorRef], Option[String])]): Unit = {
      logger.debug(s"\nUpdating neighborhood with neighbors $neighbors")
      this.nbrs.keySet.diff(neighbors.keySet).foreach(removeNeighbor)
      neighbors.keySet.diff(this.nbrs.keySet).foreach(
        nbr => this.nbrs += nbr -> NbrInfo(nbr, None, neighbors(nbr)._1, neighbors(nbr)._2))
    }
  }

  trait P2pBaseDeviceActor
    extends DynamicComputationDeviceActor
    with P2pNbrManagementBehavior {

    override def propagateMsgToNeighbors(msg: Any): Unit = {
      import context.dispatcher
      val NBR_LOOKUP_TIMEOUT = 2.seconds

      nbrs.foreach { case (idn, NbrInfo(_, _, mailboxOpt, pathOpt)) =>
        mailboxOpt match {
          // If we have a mailbox reference, we can use it directly
          case Some(ref) => ref ! msg
          // If we have a path, we can try to lookup the mailbox reference
          case None => pathOpt.foreach { path =>
            this.context.system.actorSelection(path).resolveOne(NBR_LOOKUP_TIMEOUT).onComplete {
              // Lookup success: we can use the reference
              case Success(nRef) => self ! MsgDeviceLocation(idn, nRef)
              // Lookup failure: what should we do?
              // Should we remove the neighbor after some tries?
              case Failure(e) => //self ! MsgRemoveNeighbor(idn)
            }
          }
        }
      }
    }
  }
}
