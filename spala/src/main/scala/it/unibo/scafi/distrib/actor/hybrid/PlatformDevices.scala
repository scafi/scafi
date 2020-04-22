/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.hybrid

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.distrib.actor.p2p.PlatformBehaviors
import scala.concurrent.duration._
import scala.language.postfixOps

trait PlatformDevices extends PlatformBehaviors { self: HybridPlatform.Subcomponent =>
  /**
    * Specializes a [[ComputationDeviceActor]] to work both in a decentralized,
    *  peer-to-peer manner and as a part of a client/server system.
    * In particular, it needs to propagate each computed state to its neighbors.
    */
  class HybridDeviceActor(override val selfId: UID,
                          override var aggregateExecutor: Option[ProgramContract],
                          override var execScope: ExecScope,
                          val server: ActorRef)
    extends P2pBaseDeviceActor
    with MissingCodeManagementBehavior
    with ObservableDeviceActor {

    import context.dispatcher
    context.system.scheduler.schedule(
      initialDelay = 0 seconds,
      interval = 1 second,
      receiver = server,
      message = MsgGetNeighborhoodLocations(selfId))

    override def preStart(): Unit = {
      super.preStart()
      server ! MsgRegistration(selfId)
    }
  }

  object HybridDeviceActor extends Serializable {
    def props(selfId: UID,
              program: Option[ProgramContract],
              execStrategy: ExecScope,
              server: ActorRef): Props =
      Props(classOf[HybridDeviceActor], self, selfId, program, execStrategy, server)
  }
}

