/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples.gui.hybrid

import akka.actor.{ActorRef, Props}
import examples.gui.{DevViewActor => AbstractDevViewActor}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation

class DevViewActor(override val I: BasicAbstractActorIncarnation, override var dev: ActorRef)
  extends AbstractDevViewActor

object DevViewActor {
  val DevicesInRow: Int = AbstractDevViewActor.DevicesInRow
  def props(inc: BasicAbstractActorIncarnation, dev: ActorRef): Props = Props(classOf[DevViewActor], inc, dev)
}
