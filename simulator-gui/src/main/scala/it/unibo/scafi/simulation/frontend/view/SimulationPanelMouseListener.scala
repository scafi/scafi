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

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import java.awt.event.{MouseAdapter, MouseEvent}

import it.unibo.scafi.simulation.frontend.controller.Controller

/**
  * This is the SimulationPanel's mouse listener
  */
class SimulationPanelMouseListener private[view](val panel: SimulationPanel) extends MouseAdapter {
  // private final ControllerView controllerView = ControllerView.getIstance();
  final private var captureRect: Rectangle = null
  private[view] val controller: Controller = Controller.getInstance
  final private val start: Point = new Point // End point of the selection
  private var flag: Boolean = false          // Indicates if we have pressed on a selected area

  captureRect = panel.getCaptureRect

  override def mouseClicked(e: MouseEvent) {
    super.mouseClicked(e)
    val a: Double = captureRect.x + captureRect.getWidth
    val b: Double = captureRect.y + captureRect.getHeight
    if (e.getX > captureRect.x && e.getX < a && e.getY > captureRect.y && e.getY < b) {
      flag = true
    }
    else {
      flag = false
    }
    if (!flag) {
      // Let's deselect all the nodes
      controller.getNodes.foreach(_._2.setSelected(false))
      captureRect.setBounds(0, 0, 0, 0) // Let's create a new selection area
      panel.maybeShowPopup(e) // Let's hide the menu
      panel.repaint()
    }
  }

  override def mousePressed(e: MouseEvent) {
    val a: Double = captureRect.x + captureRect.getWidth
    val b: Double = captureRect.y + captureRect.getHeight
    if (!e.isMetaDown) {
      start.x = e.getX
      start.y = e.getY
      if (e.getX > captureRect.x && e.getX < a && e.getY > captureRect.y && e.getY < b) {
        flag = true
      }
      else {
        flag = false
      }
    }
    panel.maybeShowPopup(e)
  }

  override def mouseReleased(e: MouseEvent) {
    panel.maybeShowPopup(e)
  }

  override def mouseDragged(me: MouseEvent) {
    if (flag) {
      // Dragging a selected area
      moveRectangle(me)
    }
    else {
      // Dragging to expand the area to be selected
      val end: Point = me.getPoint
      if (end.x < start.x && end.y < start.y) {
        captureRect.setRect(end.x, end.y, start.x - end.x, start.y - end.y)
      }
      else if (end.getX > start.getX && end.getY < start.getY) {
        val start1: Point = new Point(start.x, end.y)
        captureRect.setRect(start1.x, start1.y, end.x - start.x, start.y - end.y)
      }
      else if (end.x > start.x && end.y > start.y) {
        captureRect.setRect(start.x, start.y, end.x - start.x, end.y - start.y)
      }
      else if (end.getX < start.getX && end.getY > start.getY) {
        val start1: Point = new Point(end.x, start.y)
        captureRect.setRect(start1.x, start1.y, start.x - end.x, end.y - start.y)
      }
      panel.setRectSelection(captureRect)
      panel.repaint()
      controller.selectNodes(captureRect) // Selects nodes within area
    }
  }

  def moveRectangle(e: MouseEvent) {
    if (!e.isMetaDown) {
      captureRect.setLocation(captureRect.getLocation.x + e.getX - start.x, captureRect.getLocation.y + e.getY - start.y)
      panel.setRectSelection(captureRect)
      controller.moveNodeSelect(new Point(e.getX - start.x, e.getY - start.y))
      start.x = e.getX
      start.y = e.getY
      panel.repaint()
    }
  }
}
