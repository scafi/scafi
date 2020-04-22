/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import java.awt.event.{MouseAdapter, MouseEvent}

import it.unibo.scafi.simulation.frontend.controller.Controller

/**
  * This class represent an ActionListener for a GuiNode
  */
class GuiNodeListeners private[view](val node: GuiNode) extends MouseAdapter {
  final private val controller: Controller = Controller.getInstance
  final private val p: Point = new Point

  override def mouseClicked(e: MouseEvent) {
    super.mouseClicked(e)
    if (e.getButton == MouseEvent.BUTTON3) {
      repositionsInfoPanel()
    }
  }

  override def mousePressed(e: MouseEvent) {
    super.mousePressed(e)
    if (!e.isMetaDown) {
      p.x = e.getX
      p.y = e.getY
    }
  }

  override def mouseDragged(e: MouseEvent) {
    super.mouseDragged(e)
    if (!e.isMetaDown) {
      val pos: Point = node.getLocation
      node.setNodeLocation(pos.x + e.getX - p.x, pos.y + e.getY - p.y)
      if (node.getInfoPanel != null) {
        repositionsInfoPanel()
      }
      controller.moveNode(node, pos)
    }
  }

  private def repositionsInfoPanel() {
    controller.showInfoPanel(node, true)
    val infoP: NodeInfoPanel = node.getInfoPanel
    val screen: Dimension = Toolkit.getDefaultToolkit.getScreenSize
    if (node.getX > screen.getWidth / 2) {
      infoP.setLocation(node.getX - infoP.getSize().width, node.getY)
    }
    else {
      infoP.setLocation(node.getX + node.getSize().width, node.getY)
    }
    if (node.getY > (screen.getHeight / 1.5)) {
      infoP.setLocation(node.getX, node.getY - infoP.getHeight)
    }
  }
}
