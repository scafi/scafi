package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import javafx.beans.Observable

import com.sun.javafx.perf.PerformanceTracker
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.IntLog

import scalafx.Includes._
import scalafx.animation.TranslateTransition
import scalafx.application.Platform
import scalafx.geometry.Bounds
import scalafx.scene.input._
import scalafx.scene.layout.{Pane, Region}
import scalafx.scene.shape.Rectangle
import scalafx.scene.{Node, Scene}
import scalafx.util.Duration

/**
  * a set of strategy to add functionality to javafx pane
  */
private [scalaFX] object PaneExtension {
  private val ScaleFactor = 1.1
  private val minZoomOut = 0.5

  /**
    * bind the region size to scene
    * @param node the region
    * @param percentageWidth
    * @param percentageHeight
    * @param scene
    */
  def bindSize(node : Region,percentageWidth : Double = 1, percentageHeight : Double = 1) (implicit scene : Scene): Unit = {
    bindHeight()
    bindWidth()

    scene.width.addListener((o : Observable) => bindWidth())
    scene.height.addListener((o : Observable) => bindHeight())

    def bindHeight(): Unit = {
      node.minHeight = scene.height.value * percentageHeight
      node.maxHeight = scene.height.value * percentageHeight
    }

    def bindWidth () : Unit = {
      node.maxWidth = scene.width.value * percentageWidth
      node.minWidth = scene.width.value * percentageWidth
    }
  }

  /**
    * clip pane panned
    * @param pane the pane
    */
  def clip(pane : Pane) : Unit = {
    val rect = new Rectangle()
    pane.clip = rect
    pane.heightProperty().addListener((o : Observable) => rect.height = pane.height.value)
    pane.widthProperty().addListener((o : Observable) => rect.width = pane.width.value)
  }

  /**
    * allow to crate a zoomable pane
    * @param outerNode the node to attach event zoom listener
    * @param innerNode the pane where zoom act
    */
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

  /**
    * create a draggable pane
    * @param pane where drag action can do
    */
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

  /**
    * allow to track scene fps
    * @param scene the scene
    */
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

  /**
    * allow to hide and show a panel
    * @param outerNode the node where hide node is located
    * @param hideNode the node to hide
    * @param offsetX the offsetX to move hideNode
    * @param offsetY the offsetY to move hideNode
    * @param keyCombination the combination to hide / show the hideNode
    */
  def showHidePanel(outerNode : Node,
                    hideNode : Node,
                    offsetX : Double,
                    offsetY : Double,
                    keyCombination: KeyCombination): Unit = {

    val translate = new TranslateTransition(Duration.apply(1000),hideNode)
    hideNode.translateX = -offsetX
    hideNode.translateY = -offsetY
    outerNode.handleEvent(KeyEvent.KeyPressed) {
      var hide = true
      key : KeyEvent => if(keyCombination.`match`(key)) {
        if(hide) {
          translate.toX = 0
          translate.toY = 0
          translate.play()
          hideNode.requestFocus()
        } else {
          translate.toX = -offsetX
          translate.toY = -offsetY
          translate.play()
        }
        hide = !hide
      }
    }
  }
}
