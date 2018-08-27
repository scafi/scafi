package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import javafx.beans.Observable

import com.sun.javafx.perf.PerformanceTracker
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.IntLog

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Bounds
import scalafx.scene.{Node, Scene}
import scalafx.scene.input.{MouseButton, MouseEvent, ScrollEvent}
import scalafx.scene.layout.Pane
import scalafx.scene.shape.Rectangle
import scalafx.stage.Stage
object PaneExtension {
  private val ScaleFactor = 1.1
  private val minZoomOut = 0.5

  def bindSize(node : Pane,pw : Double = 1, ph : Double = 1) (implicit scene : Scene): Unit = {
    bindHeight()
    bindWidth()

    scene.width.addListener((o : Observable) => bindWidth())
    scene.height.addListener((o : Observable) => bindHeight())

    def bindHeight(): Unit = {

      node.minHeight = scene.height.value * ph
      node.maxHeight = scene.height.value * ph
    }

    def bindWidth () : Unit = {
      node.maxWidth = scene.width.value * pw
      node.minWidth = scene.width.value * pw
    }
  }

  def clip(pane : Pane) : Unit = {
    val rect = new Rectangle()
    pane.clip = rect
    pane.heightProperty().addListener((o : Observable) => rect.height = pane.height.value)
    pane.widthProperty().addListener((o : Observable) => rect.width = pane.width.value)
  }

  def zoomPane(outerNode : Node, innerNode : Node) : Unit = {
    require(innerNode != null && outerNode != null)

      outerNode.onScroll = (e : ScrollEvent) => {
      val scaleFactor = if (e.getDeltaY() > 0) ScaleFactor else 1 / ScaleFactor
      if (!(scaleFactor < ScaleFactor && innerNode.scaleX.value < minZoomOut)) {
        val oldScale: Double = innerNode.getScaleX
        val scale: Double = oldScale * scaleFactor
        val f: Double = (scale / oldScale) - 1

        // determine offset that we will have to move the node
        val bounds: Bounds = innerNode.localToScene(innerNode.getBoundsInLocal)
        val dx: Double = (e.x - ((bounds.getWidth / 2) + bounds.getMinX))
        val dy: Double = (e.y - ((bounds.getHeight / 2) + bounds.getMinY))
        // timeline that scales and moves the node
        innerNode.translateX = innerNode.translateX.value - f * dx
        innerNode.translateY = innerNode.translateY.value - f * dy
        innerNode.scaleX = scale
        innerNode.scaleY = scale
      }
    }
  }

  def dragPane(pane : Node) : Unit = {require(pane != null)
    var onDrag = false
    val minScale = 1
    var x, y: Double = 0
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
    pane.onMouseReleased = (e : MouseEvent) => onDrag = false}

  def trackFps(scene : Scene) : Unit = {
    val fpsChannel = "fps"
    val timeToWait = 1000
    val tracker = PerformanceTracker.getSceneTracker(scene)
    new Thread(new Runnable {
      override def run(): Unit = {
        while(true) {
          Platform.runLater {
            LogManager.notify(IntLog(fpsChannel,"instant",tracker.getInstantFPS.toInt))
          }
          Thread.sleep(timeToWait)
        }
      }
    }).start()
  }

}
