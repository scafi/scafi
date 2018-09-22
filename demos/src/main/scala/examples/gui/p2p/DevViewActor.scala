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

package examples.gui.p2p

import akka.actor.{ActorRef, Props}
import examples.gui.AbstractDevViewActor
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation

import scala.concurrent.duration._
import scala.language.postfixOps

class DevViewActor(override val I: BasicAbstractActorIncarnation, override var dev: ActorRef) extends AbstractDevViewActor {
  import context.dispatcher
  context.system.scheduler.schedule(1 seconds, 500 milliseconds, self, "tick")

  override def receive: Receive = ({
    case "tick" => devsGUIActor ! I.MsgGetNeighborhood(devComponent.id)
    case n: I.MsgNeighborhoodUpdate => dev ! n
  }: Receive) orElse super.receive
}

object DevViewActor {
  val DevicesInRow: Int = AbstractDevViewActor.DevicesInRow
  def props(inc: BasicAbstractActorIncarnation, dev: ActorRef): Props = Props(classOf[DevViewActor], inc, dev)
}
