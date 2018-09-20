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

package it.unibo.scafi.distrib.actor.view

import java.awt.{Dimension, Toolkit}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import javax.swing._

private class DevsGUIActor extends Actor {
  protected val Log = akka.event.Logging(context.system, this)
  private val frame = new JFrame("Devices GUI")

  private val dim: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  frame.setBounds(0, 0, dim.getWidth.toInt, dim.getHeight.toInt)
  frame.setLocationRelativeTo(null)
  frame.setLayout(null)
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

  override def receive: Receive = {
    case m: MsgAddDevComponent =>
      if (!frame.isVisible) frame.setVisible(true)
      frame.add(m.devComponent)
      frame.revalidate(); frame.repaint()
    case msg => Log.debug("[DevsGUIActor] Message unhandled: " + msg); unhandled(msg)
  }
}

object DevicesGUI {
  private var _actor: Option[ActorRef] = None
  def setupGui(actorSys: ActorSystem): Unit = if (_actor.isEmpty) _actor = Some(actorSys.actorOf(Props[DevsGUIActor]))
  def actor: Option[ActorRef] = _actor
}

trait DevComponent extends JComponent {
  type ID
  type EXPORT
  type LSNS

  var id: ID = _
  var export: EXPORT = _
  var sensors: Map[LSNS, Any] = _
}
