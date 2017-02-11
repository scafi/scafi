package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Controller
import javax.swing._
import java.awt._
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.beans.PropertyVetoException

/**
  * This is the SimulationPanel's mouse listener
  * Created by Varini on 07/11/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class SimulationPanelMouseListener private[view](val panel: SimulationPanel) extends MouseAdapter {
  // private final ControllerView controllerView = ControllerView.getIstance();
  final private var captureRect: Rectangle = null
  private[view] val controller: Controller = Controller.getIstance
  final private val point: Point = new Point
  private var flag: Boolean = false
  private[view] var start: Point = new Point

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
      captureRect.setBounds(0, 0, 0, 0) //= new Rectangle(); // Let's create a new selection area
      panel.maybeShowPopup(e) // Let's hide the menu
      panel.repaint()
    }
  }

  override def mouseMoved(me: MouseEvent) {
    start = me.getPoint
  }

  override def mousePressed(e: MouseEvent) {
    val a: Double = captureRect.x + captureRect.getWidth
    val b: Double = captureRect.y + captureRect.getHeight
    if (!e.isMetaDown) {
      point.x = e.getX
      point.y = e.getY
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
      moveRectangle(me)
    }
    else {
      val end: Point = me.getPoint
      if (end.x < start.x && end.y < start.y) {
        captureRect.setRect(end.x, end.y, start.x - end.x, start.y - end.y) // = new Rectangle(end, new Dimension(start.x - end.x, start.y - end.y));
      }
      else if (end.getX > start.getX && end.getY < start.getY) {
        val start1: Point = new Point(start.x, end.y)
        captureRect.setRect(start1.x, start1.y, end.x - start.x, start.y - end.y) //= new Rectangle(start1, new Dimension(end.x - start.x, start.y - end.y));
      }
      else if (end.x > start.x && end.y > start.y) {
        captureRect.setRect(start.x, start.y, end.x - start.x, end.y - start.y) // = new Rectangle(start, new Dimension(end.x - start.x, end.y - start.y));
      }
      else if (end.getX < start.getX && end.getY > start.getY) {
        val start1: Point = new Point(end.x, start.y)
        captureRect.setRect(start1.x, start1.y, start.x - end.x, end.y - start.y) // = new Rectangle(start1, new Dimension(start.x - end.x, end.y - start.y));
      }
      panel.setRectSelection(captureRect)
      panel.repaint()
      controller.selectNodes(captureRect) // Selects nodes within area
    }
  }

  def moveRectangle(e: MouseEvent) {
    if (!e.isMetaDown) {
      captureRect.setLocation(captureRect.getLocation.x + e.getX - point.x, captureRect.getLocation.y + e.getY - point.y)
      panel.setRectSelection(captureRect)
      controller.moveNodeSelect(new Point(e.getX - point.x, e.getY - point.y))
      point.x = e.getX
      point.y = e.getY
      panel.repaint()
    }
  }
}