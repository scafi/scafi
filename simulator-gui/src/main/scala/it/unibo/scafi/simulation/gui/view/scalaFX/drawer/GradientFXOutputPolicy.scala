package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice

import scalafx.scene.paint.Color

object GradientFXOutputPolicy extends FXOutputPolicy {
  var maxValue = 1000.0
  val maxColor = 255.0
  override type OUTPUTNODE = javafx.scene.shape.Shape

  override def deviceToGraphicsNode(node: OUTPUTNODE, dev: DEVICE): Option[OUTPUTNODE] = None

  override def updateDevice(node: OUTPUTNODE, dev: DEVICE, graphicsDevice: Option[OUTPUTNODE]): Unit = {
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
  override def nodeGraphicsNode (node: NODE): OUTPUTNODE = nodeToShape.create(node)
}
