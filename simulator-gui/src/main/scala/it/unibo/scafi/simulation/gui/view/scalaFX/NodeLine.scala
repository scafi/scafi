package it.unibo.scafi.simulation.gui.view.scalaFX

import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Line

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
  this.startX.bind(start.translateX + ps.x)
  startY.bind(start.translateY + ps.y)
  endX.bind(end.translateX + pend.x)
  endY.bind(end.translateY + pend.y)
  this.smooth = false
  stroke = c
}
