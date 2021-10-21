package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer

import javafx.scene.paint.ImagePattern

import it.unibo.scafi.simulation.s2.frontend.model.core.World
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.s2.frontend.view.ViewSetting
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX._

import scalafx.scene.image.Image
import scalafx.scene.shape.Shape
import scalafx.Includes._
import scalafx.scene.paint.{Color, Paint}

object SenseImageFXOutput extends FXOutputPolicy {
  private var senseToImage : Map[Any,ImagePattern] = Map.empty
  private val standardFill : Paint = Color.Black
  initializeScalaFXPlatform()
  def addRepresentation(sens : Any, file : String) = senseToImage += sens -> new ImagePattern(new Image(file))

  private lazy val image = new Image(ViewSetting.nodeImagePath)

  override type OUTPUT_NODE = StandardFXOutput.OUTPUT_NODE

  override def nodeGraphicsNode(node: World#Node): OUTPUT_NODE = StandardFXOutput.nodeGraphicsNode(node)

  override def deviceToGraphicsNode(node: OUTPUT_NODE, dev: ImageFXOutput.DEVICE): Option[OUTPUT_NODE] = {
    dev match {
      case SensorDevice(sensor) => sensor.value[Any] match {
        case led : Boolean => updateNodeWithImage(node,sensor.name,led)
        case _ =>
      }
    }
    None
  }



  override def updateDevice(node: OUTPUT_NODE, dev: ImageFXOutput.DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit =
    deviceToGraphicsNode(node,dev)


  private def updateNodeWithImage(node : OUTPUT_NODE, name : Any, led : Boolean): Unit = {
    val fxnode : Shape = node.asInstanceOf[javafx.scene.shape.Shape]
    if(!led) fxnode.fill = standardFill
    else fxnode.fill = this.senseToImage.get(name) match {
      case Some(value) => value
      case _ => standardFill
    }
  }
}

