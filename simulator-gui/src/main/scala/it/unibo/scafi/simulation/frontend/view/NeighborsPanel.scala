/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import javax.swing._

import it.unibo.scafi.simulation.frontend.Settings
import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.utility.Utils

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
    if(Settings.Sim_DrawConnections) {
      this.removeAll()
      g.setColor(Settings.Color_link)
      controller.getNeighborhood.foreach(kv => {
        val (n, gn) = kv
        val p1 = Utils.calculatedGuiNodePosition(n.position2d)
        val p1x = (p1.x + (Utils.getSizeGuiNode().getWidth() / 2))
        val p1y = (p1.y + (Utils.getSizeGuiNode().getHeight() / 160 * 71))
        gn.foreach(nbr => {
          val p2 = Utils.calculatedGuiNodePosition(nbr.position2d)
          val p2x = (p2.x + (Utils.getSizeGuiNode().getWidth() / 2))
          val p2y = (p2.y + (Utils.getSizeGuiNode().getHeight() / 160 * 71))
          g.drawLine(p1x.toInt, p1y.toInt, p2x.toInt, p2y.toInt)
        })
      })
    }
  }
}
