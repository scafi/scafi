package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer
import javafx.scene.paint.ImagePattern

import it.unibo.scafi.simulation.s2.frontend.model.core.World
import it.unibo.scafi.simulation.s2.frontend.view.ViewSetting

import scalafx.Includes._
import scalafx.scene.image.Image
import scalafx.scene.shape.Shape
/**
 * show node using image specified in view setting device are showed using {@see StandardFXOutput}
 */
case object ImageFXOutput extends FXOutputPolicy {
  // load drone image
  private lazy val image = new Image(ViewSetting.nodeImagePath)
  private lazy val pattern = new ImagePattern(image)
  override type OUTPUT_NODE = StandardFXOutput.OUTPUT_NODE

  override def nodeGraphicsNode(node: World#Node): OUTPUT_NODE = {
    val res: Shape = StandardFXOutput.nodeGraphicsNode(node).asInstanceOf[javafx.scene.shape.Shape]
    res.fill = pattern
    res
  }

  override def deviceToGraphicsNode(node: OUTPUT_NODE, dev: ImageFXOutput.DEVICE): Option[OUTPUT_NODE] =
    StandardFXOutput.deviceToGraphicsNode(node, dev)

  override def updateDevice(node: OUTPUT_NODE, dev: ImageFXOutput.DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit =
    StandardFXOutput.updateDevice(node, dev, graphicsDevice)
}
