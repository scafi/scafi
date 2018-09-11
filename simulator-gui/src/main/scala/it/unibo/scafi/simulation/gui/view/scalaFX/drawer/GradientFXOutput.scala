package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.scalaFX.{ScalaFXEnvironment, modelShapeToFXShape}

import scalafx.scene.paint.Color

/**
  * a output strategy that associated a color to
  * number, allow to see graphically the result
  * of computation
  */
case object GradientFXOutput extends FXOutputPolicy {
  lazy val maxValue : Float = WindowConfiguration.toWindowRect(ScalaFXEnvironment.windowConfiguration).w
  val maxColor = 255.0
  override type OUTPUT_NODE = javafx.scene.shape.Shape

  override def deviceToGraphicsNode(node: OUTPUT_NODE, dev: DEVICE): Option[OUTPUT_NODE] = None

  override def updateDevice(node: OUTPUT_NODE, dev: DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit = {
    dev match {
      case SensorDevice(sens) => sens.value match {
        case numeric : Double => node.fillProperty().setValue(doubleToColor(numeric))
        case _ =>
      }
      case _ => None
    }
  }
  private implicit def doubleToColor(v : Double) : Color  = {
    if(v > maxValue) {
      Color.Black
    } else {
      val color = ((maxColor / maxValue) * v - 255) * -1
      Color.rgb(color.toInt,0,0)
    }

  }
  override def nodeGraphicsNode (node: NODE): OUTPUT_NODE = modelShapeToFXShape.apply(node.shape,node.position)
}
