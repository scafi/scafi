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
import it.unibo.scafi.distrib.actor.p2p.PlatformBehaviors
import scala.concurrent.duration._
import scala.language.postfixOps

trait PlatformDevices extends PlatformBehaviors { self: Platform.Subcomponent =>
  /**
    * Specializes a [[ComputationDeviceActor]] to work both in a decentralized,
    *  peer-to-peer manner and as a part of a client/server system.
    * In particular, it needs to propagate each computed state to its neighbors.
    */
  class DeviceActor(override val selfId: UID,
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

  object DeviceActor extends Serializable {
    def props(selfId: UID,
              program: Option[ProgramContract],
              execStrategy: ExecScope,
              server: ActorRef): Props =
      Props(classOf[DeviceActor], self, selfId, program, execStrategy, server)
  }
}

