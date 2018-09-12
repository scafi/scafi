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
               c : Color) extends Line{
  update()
  this.smooth = false
  stroke = c

  /**
    * update the position of the line extreme
    */
  def update() : Unit = {
    val ps = nodeToPosition(start)
    val pend = nodeToPosition(end)
    startX = ps.x
    startY = ps.y
    endX = pend.x
    endY = pend.y
  }
}
