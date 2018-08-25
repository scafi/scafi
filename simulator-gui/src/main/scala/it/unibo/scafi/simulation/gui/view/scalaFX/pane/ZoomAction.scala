package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import scalafx.animation.{KeyFrame, KeyValue, Timeline}
import scalafx.geometry.Bounds
import scalafx.scene.layout.Pane
import scalafx.util.Duration
import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.input.ScrollEvent

object ZoomAction {
  private val ScaleFactor = 1.1
  private val minZoomOut = 0.5

  def apply(outerNode : Node, innerNode : Node) : Unit = {
    require(innerNode != null && outerNode != null)
    outerNode.onScroll = (e : ScrollEvent) => {
      val scaleFactor = if (e.getDeltaY() > 0) ScaleFactor else 1 / ScaleFactor
      if (!(scaleFactor < ScaleFactor && innerNode.scaleX.value < minZoomOut)) {
        innerNode.setScaleX(innerNode.getScaleX() * scaleFactor);
        innerNode.setScaleY(innerNode.getScaleY() * scaleFactor);
        val oldScale: Double = innerNode.getScaleX
        val scale: Double = oldScale * scaleFactor
        val f: Double = (scale / oldScale) - 1

        // determine offset that we will have to move the node
        val bounds: Bounds = innerNode.localToScene(innerNode.getBoundsInLocal)
        val dx: Double = (e.sceneX - (bounds.getWidth + bounds.getMinX))
        val dy: Double = (e.sceneY - (bounds.getHeight + bounds.getMinY))
        // timeline that scales and moves the node
        innerNode.translateX = innerNode.translateX.value - f * dx
        innerNode.translateY = innerNode.translateY.value - f * dy
        innerNode.scaleX = scale
        innerNode.scaleY = scale
      }
    }
  }
}
