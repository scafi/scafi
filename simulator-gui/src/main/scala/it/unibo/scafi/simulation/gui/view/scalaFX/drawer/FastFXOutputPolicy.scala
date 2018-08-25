package it.unibo.scafi.simulation.gui.view.scalaFX.drawer
import javafx.scene.paint.Color

import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{SensorDevice, sensorOutput}


object FastFXOutputPolicy extends FXOutputPolicy {

  override type OUTPUT_NODE = javafx.scene.shape.Shape

  override def nodeGraphicsNode (node: NODE): OUTPUT_NODE = nodeToShape.create(node)

  override def deviceToGraphicsNode (node: OUTPUT_NODE, dev: DEVICE): Option[OUTPUT_NODE] = None

  override def updateDevice(node : OUTPUT_NODE, dev: DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit = {
    import scalafx.Includes._
    dev match {
      case SensorDevice(sens) => sens.value match {
        case led : Boolean => {
          if (led) {
            val color = StandardFXOutputPolicy.colors(dev.name.toString)
            if (sens.stream == sensorOutput && node.fillProperty().value == Color.BLACK) {
              node.fillProperty().setValue(color)
            } else if (sens.stream != sensorOutput) {
              node.fillProperty().setValue(color)
            }
          } else {
            if (jfxPaint2sfx(node.fillProperty().value) == StandardFXOutputPolicy.colors(dev.name.toString)) {
              node.fillProperty().setValue(Color.BLACK)
            }
          }
        }
        case _ =>
      }
    }
  }

  override def toString: String = "fast-fx"
}
