package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer

import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.s2.frontend.view.WindowConfiguration
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.{ScalaFXEnvironment, modelShapeToFXShape}

import scalafx.scene.paint.Color

/**
  * a output strategy that associated a color to
  * number, allow to see graphically the result
  * of computation
  */
case object GradientFXOutput extends FXOutputPolicy {
  import WindowConfiguration._
  private lazy val maxValue = ScalaFXEnvironment.windowConfiguration.w
  private val maxColor = 360
  private val saturation = 1
  private val light = 1
  override type OUTPUT_NODE = javafx.scene.shape.Shape

  override def deviceToGraphicsNode(node: OUTPUT_NODE, dev: DEVICE): Option[OUTPUT_NODE] = None

  override def updateDevice(node: OUTPUT_NODE, dev: DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit = {
    dev match {
      case SensorDevice(sens) => sens.value[Any] match {
        case number : Number => node.fillProperty().setValue(numberToColor(number))
        case _ =>
      }
      case _ => None
    }
  }
  private implicit def numberToColor(v : Number) : Color  = {
    val doubleValue : Double = v.doubleValue()
    Color.hsb((doubleValue / maxValue) * maxColor,saturation,light)
  }
  override def nodeGraphicsNode (node: NODE): OUTPUT_NODE = modelShapeToFXShape.apply(node.shape,node.position)
}
