package it.unibo.scafi.simulation.gui.view.scalaFX.drawer
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{SensorDevice, sensorOutput}
import it.unibo.scafi.simulation.gui.view.ViewSetting._
import it.unibo.scafi.simulation.gui.view.scalaFX.modelShapeToFXShape

/**
  * a output strategy that show led sensor:
  * show the led with changing node color,
  * output node using {@see StandardFXOuput}
  */
case object FastFXOutput extends FXOutputPolicy {

  override type OUTPUT_NODE = javafx.scene.shape.Shape

  override def nodeGraphicsNode (node: NODE): OUTPUT_NODE = modelShapeToFXShape.apply(node.shape,node.position)

  override def deviceToGraphicsNode (node: OUTPUT_NODE, dev: DEVICE): Option[OUTPUT_NODE] = None

  override def updateDevice(node : OUTPUT_NODE, dev: DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit = {
    import scalafx.Includes._
    dev match {
      case SensorDevice(sens) => sens.value[Any] match {
        case led : Boolean =>
          val index = deviceName.indexOf(dev.name.toString)
          if (led) {
            val color = deviceColor(index % deviceColor.size)
            if (sens.stream == sensorOutput && jfxPaint2sfx(node.fillProperty().value) == nodeColor) {
              node.fillProperty().setValue(color)
            } else if (sens.stream != sensorOutput) {
              node.fillProperty().setValue(color)
            }
          } else {
            if (jfxPaint2sfx(node.fillProperty().value) == deviceColor(index % deviceColor.size)) {
              node.fillProperty().setValue(nodeColor)
            }
          }
        case _ =>
      }
    }
  }
}
