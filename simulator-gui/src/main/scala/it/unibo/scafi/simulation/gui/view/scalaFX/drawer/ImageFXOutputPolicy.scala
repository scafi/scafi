package it.unibo.scafi.simulation.gui.view.scalaFX.drawer
import javafx.scene.paint.ImagePattern

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.ViewSetting

import scalafx.scene.image.Image
import scalafx.scene.shape.Shape
import scalafx.Includes._
/**
  * show node like a drone
  */
object ImageFXOutputPolicy extends FXOutputPolicy {
  //load drone imege
  private lazy val image = new Image(ViewSetting.nodeImagePath)
  private lazy val pattern = new ImagePattern(image)
  override type OUTPUT_NODE = StandardFXOutputPolicy.OUTPUT_NODE

  override def nodeGraphicsNode(node: World#Node): OUTPUT_NODE = {
    val res : Shape = StandardFXOutputPolicy.nodeGraphicsNode(node).asInstanceOf[javafx.scene.shape.Shape]
    res.fill = pattern
    res
  }

  override def deviceToGraphicsNode(node: OUTPUT_NODE, dev: ImageFXOutputPolicy.DEVICE): Option[OUTPUT_NODE] =
    StandardFXOutputPolicy.deviceToGraphicsNode(node,dev)

  override def updateDevice(node: OUTPUT_NODE, dev: ImageFXOutputPolicy.DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit =
    StandardFXOutputPolicy.updateDevice(node,dev,graphicsDevice)
}
