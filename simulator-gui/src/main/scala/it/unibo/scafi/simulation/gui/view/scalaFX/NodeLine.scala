package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.beans.{InvalidationListener, Observable}
import javafx.beans.binding.DoubleBinding

import scalafx.beans.binding
import scalafx.beans.binding.Bindings
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Line
import scalafx.Includes._

/**
  * a line that link two node
  * @param start start node
  * @param end end node
  * @param c the line color
  */
private [scalaFX] class NodeLine(private val start : Node,
               private val end : Node,
               c : Color) extends Line {
  private val ps = nodeToAbsolutePosition(start)
  private val pend = nodeToAbsolutePosition(end)
  startX <== start.translateX + ps.x
  startY <== start.translateY + ps.y
  endX <== end.translateX + pend.x
  endY <== end.translateY + pend.y
  this.smooth = false
  stroke = c

  def unbind(): Unit = {
    this.visible = false
  }
}
