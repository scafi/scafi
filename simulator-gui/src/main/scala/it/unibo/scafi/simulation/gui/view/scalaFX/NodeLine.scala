package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.beans.value.{ChangeListener, ObservableValue}

import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Line

/**
  *
  * @param start start node
  * @param end end node
  */
class NodeLine(private val start : Node,
               private val end : Node,
               c : Color) extends Line{
  val ps = nodeToAbsolutePosition(start)
  val pend = nodeToAbsolutePosition(end)
  update()
  this.smooth = false
  stroke = c

  def update() : Unit = {
    startX = start.translateX.value + ps.x
    startY = start.translateY.value + ps.y
    endX = end.translateX.value + pend.x
    endY = end.translateY.value + pend.y
  }
}
