package it.unibo.scafi.distrib.actor.p2p

import akka.actor.{Actor, Props}

import scala.util.{Failure, Success}
import scala.concurrent.duration._

/**
 * @author Roberto Casadei
 *
 */

trait PlatformDevices { self: Platform.Subcomponent =>

  /**
   * Neighbourhood management for devices in a P2P platform.
   */
  trait P2pNbrManagementBehavior extends BaseNbrManagementBehavior { selfActor: Actor =>
    def NeighbourhoodManagementBehavior: Receive = {
      case info @ NbrInfo(idn,_,_,_) => mergeNeighborInfo(idn,info)
      case MsgDeviceLocation(idn, ref) => mergeNeighborInfo(idn,NbrInfo(idn,None,Some(ref),None))
      case MsgExport(from, export) => {
        mergeNeighborInfo(from, NbrInfo(from, None, Some(sender), Some(sender.path.toString)))
        updateNeighborsState(Map(from -> Some(export)))
      }
      case MsgRemoveNeighbor(idn) => removeNeighbor(idn)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(NeighbourhoodManagementBehavior)
  }

  /**
   * Specializes a [[ComputationDeviceActor]] to work in a decentralized,
   *  peer-to-peer manner.
   * In particular, it needs to propagate each computed state to its neighbors.
   */
  class DeviceActor(override val selfId: ID,
                    override var aggregateExecutor: Option[ExecutionTemplate],
                    override var execScope: ExecScope)
    extends DynamicComputationDeviceActor
    with MissingCodeManagementBehavior
    with P2pNbrManagementBehavior {

    def PropagateExportToNeighbors(export: EXPORT) = {
      import context.dispatcher

      val NBR_LOOKUP_TIMEOUT = 2 seconds

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
  }

  object DeviceActor extends Serializable {
    def props(selfId: ID,
              program: Option[ExecutionTemplate],
              execStrategy: ExecScope) =
      Props(classOf[DeviceActor], self, selfId, program, execStrategy)
  }
}
