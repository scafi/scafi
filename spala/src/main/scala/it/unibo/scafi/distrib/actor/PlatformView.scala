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

package it.unibo.scafi.distrib.actor

import java.awt.{Dimension, Toolkit}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.scafi.space.Point2D
import javax.swing.{JComponent, JFrame, WindowConstants}

trait PlatformView { self: Platform.Subcomponent =>
  class DevsGUIActor() extends Actor {
    val frame = new JFrame("Devices GUI")
    protected val Log = akka.event.Logging(context.system, this)
    private var components: Set[DevComponent] = Set()

    buildFrame()

    override def receive: Receive = {
      case m: MsgAddDevComponent =>
        if (!frame.isVisible) frame.setVisible(true)
        components = components + m.devComponent
        frame.add(m.devComponent)
        frame.revalidate(); frame.repaint()
      case n: MsgGetNeighborhood =>
        val nbrs: Set[DevComponent] = computeNeighborhood(n.id)
        sender ! MsgNeighborhoodUpdate(n.id, nbrs.map(d => d.id -> d.ref).toMap)
      case msg => Log.debug("[DevsGUIActor] Message unhandled: " + msg); unhandled(msg)
    }

    private def buildFrame(): Unit = {
      val dim: Dimension = Toolkit.getDefaultToolkit.getScreenSize
      frame.setBounds(0, 0, dim.getWidth.toInt, dim.getHeight.toInt)
      frame.setLocationRelativeTo(null)
      frame.setLayout(null)
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    }

    private def computeNeighborhood(id: UID): Set[DevComponent] = {
      def dist(p1: Point2D, p2: Point2D): Double =
        if (p1 == null || p2 == null) Double.PositiveInfinity else p1.distance(p2)
      val dev = components.find(_.id == id)
      if (dev.isDefined) {
        components.filter(d => dist(dev.get.position, d.position) <= 1 && dev.get.id != d.id)
      } else {
        Set()
      }
    }
  }

  object DevicesGUI {
    private var _actor: Option[ActorRef] = None
    def setupGui(actorSys: ActorSystem): Unit = if (_actor.isEmpty) _actor =
      Some(actorSys.actorOf(Props(classOf[DevsGUIActor], self)))
    def actor: Option[ActorRef] = _actor
  }

  trait DevComponent extends JComponent {
    var id: UID = _
    var ref: ActorRef = _
    var position: Point2D = _
  }
}
