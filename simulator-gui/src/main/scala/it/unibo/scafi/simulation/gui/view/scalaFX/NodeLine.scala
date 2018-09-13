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
  private val ps = nodeToAbsolutePosition(start)
  private val pend = nodeToAbsolutePosition(end)
  update()
  this.smooth = false
  stroke = c

  /**
    * update the position of the line extreme
    */
  def update() : Unit = {
    if(this.isVisible) {
      startX = ps.x + start.translateX.value
      startY = ps.y + start.translateY.value
      endX = pend.x + end.translateX.value
      endY = pend.y + end.translateY.value
    }

  }
}
