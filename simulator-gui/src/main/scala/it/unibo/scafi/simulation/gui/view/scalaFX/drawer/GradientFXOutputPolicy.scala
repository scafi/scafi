package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice

import scalafx.scene.paint.Color

object GradientFXOutputPolicy extends FXOutputPolicy {
  var maxValue = 1000.0
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
      val color = ((maxColor / maxValue) * v - 255) * -1;
      Color.rgb(color.toInt,0,0);
    }

  }
  override def nodeGraphicsNode (node: NODE): OUTPUT_NODE = nodeToShape.create(node)


  override def toString: String = "gradient-fx"
}
