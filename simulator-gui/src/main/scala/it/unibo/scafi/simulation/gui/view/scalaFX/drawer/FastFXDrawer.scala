package it.unibo.scafi.simulation.gui.view.scalaFX.drawer
import javafx.scene.paint.Color
import javafx.scene.shape.Shape

import it.unibo.scafi.simulation.gui.launcher.SensorName.gsensor
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.SensorDefinition.Led


object FastFXDrawer extends FXDrawer {

  override type OUTPUTNODE = javafx.scene.shape.Shape

  override def nodeGraphicsNode (node: NODE): OUTPUTNODE = nodeToShape.create(node)

  override def deviceToGraphicsNode (node: OUTPUTNODE, dev: DEVICE): Option[OUTPUTNODE] = None

  override def updateDevice(node : OUTPUTNODE, dev: DEVICE, graphicsDevice: Option[OUTPUTNODE]): Unit = {
    import scalafx.Includes._
    dev match {
      case Led(value) => {
        val shape = node.asInstanceOf[Shape]
        if (value) {
          val color = StandardFXDrawer.colors(dev.name.toString)
          if (gsensor.name == dev.name && shape.fillProperty().value == Color.BLACK) {
            node.fillProperty().setValue(color)
          } else if (gsensor.name != dev.name) {
            node.fillProperty().setValue(color)
          }
        } else {
          if (jfxPaint2sfx(shape.fillProperty().value) == StandardFXDrawer.colors(dev.name.toString)) {
            node.fillProperty().setValue(Color.BLACK)
          }
        }
      }
    }
  }
}
