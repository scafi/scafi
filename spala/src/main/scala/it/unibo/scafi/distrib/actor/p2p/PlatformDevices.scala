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

import akka.actor.{Actor, ActorRef, Props}

import scala.util.{Failure, Success}
import scala.concurrent.duration._

trait PlatformDevices { self: Platform.Subcomponent =>

  /**
   * Neighbourhood management for devices in a P2P platform.
   */
  trait P2pNbrManagementBehavior extends BaseNbrManagementBehavior { selfActor: Actor =>
    def neighbourhoodManagementBehavior: Receive = {
      case MsgNeighborhoodUpdate(_, nbs) => replaceNeighborhood(nbs)
      case info @ NbrInfo(idn,_,_,_) => mergeNeighborInfo(idn,info)
      case MsgDeviceLocation(idn, ref) => mergeNeighborInfo(idn,NbrInfo(idn,None,Some(ref),None))
      case MsgExport(from, export) => {
        mergeNeighborInfo(from, NbrInfo(from, None, Some(sender), Some(sender.path.toString)))
        updateNeighborsState(Map(from -> Some(export)))
      }
      case MsgRemoveNeighbor(idn) => removeNeighbor(idn)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(neighbourhoodManagementBehavior)

    def replaceNeighborhood(neighbors: Map[UID, ActorRef]): Unit = {
      logger.debug(s"\nUpdating neighborhood with neighbors $neighbors")

      this.nbrs.keySet.diff(neighbors.keySet).foreach(removeNeighbor)
      neighbors.keySet.diff(this.nbrs.keySet).foreach(
        nbr => this.nbrs += nbr -> NbrInfo(nbr,None,Some(neighbors(nbr))))
    }
  }

  /**
   * Specializes a [[ComputationDeviceActor]] to work in a decentralized,
   *  peer-to-peer manner.
   * In particular, it needs to propagate each computed state to its neighbors.
   */
  class DeviceActor(override val selfId: UID,
                    override var aggregateExecutor: Option[ProgramContract],
                    override var execScope: ExecScope)
    extends DynamicComputationDeviceActor
    with MissingCodeManagementBehavior
    with ObservableDeviceActor
    with P2pNbrManagementBehavior {

    def propagateExportToNeighbors(export: ComputationExport): Unit = {
      import context.dispatcher

      val NBR_LOOKUP_TIMEOUT = 2.seconds

      nbrs.foreach { case (idn, NbrInfo(_, expOpt, mailboxOpt, pathOpt)) =>
        mailboxOpt match {
          // If we have a mailbox reference, we can use it directly
          case Some(ref) => {
            ref ! MsgExport(selfId, export)
          }
          // If we have a path, we can try to lookup the mailbox reference
          case None => pathOpt.foreach { path =>
            this.context.system.actorSelection(path).resolveOne(NBR_LOOKUP_TIMEOUT).onComplete {
              // Lookup success: we can use the reference
              case Success(nref) => self ! MsgDeviceLocation(idn, nref)
              // Lookup failure: what should we do?
              // Should we remove the neighbor after some tries?
              case Failure(e) => //self ! MsgRemoveNeighbor(idn)
            }
          }
        }
      }
    }

    override def propagateLambdaToNeighbors(lambda: () => Any): Unit = {
      import context.dispatcher

      val NBR_LOOKUP_TIMEOUT = 2.seconds

      nbrs.foreach { case (idn, NbrInfo(_, expOpt, mailboxOpt, pathOpt)) =>
        mailboxOpt match {
          // If we have a mailbox reference, we can use it directly
          case Some(ref) => {
            ref ! MsgShipLambda(selfId, lambda)
          }
          // If we have a path, we can try to lookup the mailbox reference
          case None => pathOpt.foreach { path =>
            this.context.system.actorSelection(path).resolveOne(NBR_LOOKUP_TIMEOUT).onComplete {
              // Lookup success: we can use the reference
              case Success(nref) => self ! MsgDeviceLocation(idn, nref)
              // Lookup failure: what should we do?
              // Should we remove the neighbor after some tries?
              case Failure(e) => //self ! MsgRemoveNeighbor(idn)
            }
          }
        }
      }
    }
  }

  object DeviceActor extends Serializable {
    def props(selfId: UID,
              program: Option[ProgramContract],
              execStrategy: ExecScope): Props =
      Props(classOf[DeviceActor], self, selfId, program, execStrategy)
  }
}
