package it.unibo.scafi.simulation.gui.view.scalaFX

import scalafx.scene.effect.BlendMode
import scalafx.scene.{CacheHint, Node}
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Line, StrokeType}

/**
  *
  * @param start start node
  * @param end end node
  */
class NodeLine(start : Node, end : Node, c : Color) extends Line{
  val ps = nodeToAbsolutePosition(start)
  val pend = nodeToAbsolutePosition(end)
  startX.bind(start.translateX + ps.x)
  startY.bind(start.translateY + ps.y)
  endX.bind(end.translateX + pend.x)
  endY.bind(end.translateY + pend.y)
  this.smooth = false
  stroke = c //TODO COLOR MUTABLE
}
