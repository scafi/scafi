package it.unibo.scafi.simulation.gui.view.scalaFX.drawer
import javafx.scene.paint.Color
import javafx.scene.shape.Shape

import it.unibo.scafi.simulation.gui.configuration.SensorName.output1
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice


object FastFXOutputPolicy extends FXOutputPolicy {

  override type OUTPUTNODE = javafx.scene.shape.Shape

  override def nodeGraphicsNode (node: NODE): OUTPUTNODE = nodeToShape.create(node)

  override def deviceToGraphicsNode (node: OUTPUTNODE, dev: DEVICE): Option[OUTPUTNODE] = None

  override def updateDevice(node : OUTPUTNODE, dev: DEVICE, graphicsDevice: Option[OUTPUTNODE]): Unit = {
    import scalafx.Includes._
    dev match {
      case SensorDevice(sens) => sens.value match {
        case led : Boolean => {
          if (led) {
            val color = StandardFXOutputPolicy.colors(dev.name.toString)
            if (output1.name == dev.name && node.fillProperty().value == Color.BLACK) {
              node.fillProperty().setValue(color)
            } else if (output1.name != dev.name) {
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
}
