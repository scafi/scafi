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
class NodeLine(private val start : Node, private val end : Node, c : Color) extends Line{
  val ps = nodeToAbsolutePosition(start)
  val pend = nodeToAbsolutePosition(end)
  /*TODO USE BETTER UNBIND*/
  val startXL = new ChangeListener[Number] {
    override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit =
      startX = start.translateX.value + ps.x
  }
  val startYL = new ChangeListener[Number] {
    override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit =
      startY = start.translateY.value + ps.y
  }
  val endXL = new ChangeListener[Number] {
    override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit =
      endX = end.translateX.value + pend.x
  }
  val endYL = new ChangeListener[Number] {
    override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit =
      endY = end.translateY.value + pend.y
  }
  start.translateX.addListener(startXL)
  start.translateY.addListener(startYL)
  end.translateX.addListener(endXL)
  end.translateY.addListener(endYL)
  startX = start.translateX.value + ps.x
  startY = start.translateY.value + ps.y
  endX = end.translateX.value + pend.x
  endY = end.translateY.value + pend.y
  /*startX.bind(start.translateX + ps.x)
  startY.bind(start.translateY + ps.y)
  endX.bind(end.translateX + pend.x)
  endY.bind(end.translateY + pend.y)*/
  this.smooth = false
  stroke = c

  def unbind(): Unit = {
    start.translateX.removeListener(startXL)
    start.translateY.removeListener(startYL)
    end.translateX.removeListener(endXL)
    end.translateY.removeListener(endYL)
  }
}
