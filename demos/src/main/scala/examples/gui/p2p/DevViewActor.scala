/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples.gui.p2p

import akka.actor.{ActorRef, Props}
import examples.gui.{DevViewActor => AbstractDevViewActor}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation

import scala.concurrent.duration._
import scala.language.postfixOps

class DevViewActor(override val I: BasicAbstractActorIncarnation, override var dev: ActorRef) extends AbstractDevViewActor {
  case class Tick()
  import context.dispatcher
  context.system.scheduler.schedule(1 seconds, 500 milliseconds, self, Tick())

  override def receive: Receive = ({
    case Tick() => devsGUIActor ! I.MsgGetNeighborhood(id)
    case n: I.MsgNeighborhoodUpdate => dev ! n
  }: Receive) orElse super.receive
}

object DevViewActor {
  val DevicesInRow: Int = AbstractDevViewActor.DevicesInRow
  def props(inc: BasicAbstractActorIncarnation, dev: ActorRef): Props = Props(classOf[DevViewActor], inc, dev)
}
