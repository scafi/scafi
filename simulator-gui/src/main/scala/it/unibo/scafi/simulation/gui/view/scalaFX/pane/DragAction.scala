package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.layout.Pane
case class DragAction(pane : Node) {
  require(pane != null)
  private var onDrag = false
  private val minScale = 1
  private var x, y: Double = 0
  pane.onMouseDragged = (e: MouseEvent) => {
    if(onDrag) {
      val attenuationFactor: Double = 1 / (1 / pane.scaleX.value)
      pane.translateX = pane.translateX.value + attenuationFactor * (e.x - x)
      pane.translateY = pane.translateY.value + attenuationFactor * (e.y - y)
      e.consume()
    } else if(e.button == MouseButton.Secondary){
      x = e.x
      y = e.y
      onDrag = true
    }
  }
  pane.onMouseReleased = (e : MouseEvent) => onDrag = false
}
