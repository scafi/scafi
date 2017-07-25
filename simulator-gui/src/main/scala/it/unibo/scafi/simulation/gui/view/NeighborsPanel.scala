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

package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.Settings
import javax.swing._
import java.awt._

/**
  * This is the panel where are represents the connection of neighbors.
  */
class NeighborsPanel private[view]() extends JPanel {
  this.setSize(Toolkit.getDefaultToolkit.getScreenSize)
  this.setOpaque(false)
  this.setVisible(true)
  private[view] val controller: Controller = Controller.getInstance

  override protected def paintComponent(g: Graphics) {
    super.paintComponent(g)
    this.removeAll()
    g.setColor(Settings.Color_link)
    //call the neighborhood to the network object
    controller.getNeighborhood.foreach(kv => {
      val (n, nghb) = kv
      val p1 = Utils.calculatedGuiNodePosition(n.position)
      val p1x = (p1.x + (Utils.getSizeGuiNode().getWidth() / 2))
      val p1y = (p1.y + (Utils.getSizeGuiNode().getHeight() / 160 * 71 ))
      nghb.foreach(nbr => {
        val p2 = Utils.calculatedGuiNodePosition(nbr.position)
        val p2x =  (p2.x + (Utils.getSizeGuiNode().getWidth() / 2 ))
        val p2y = (p2.y + (Utils.getSizeGuiNode().getHeight() / 160 * 71 ))
        g.drawLine(p1x.toInt, p1y.toInt, p2x.toInt, p2y.toInt)
      })
    })
  }
}
