package it.unibo.scafi.simulation.gui.view.scalaFX.drawer
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Shape

import it.unibo.scafi.simulation.gui.launcher.SensorName.gsensor
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.simulation.BasicSensors.DisplaySensor.DoubleSensor
import it.unibo.scafi.simulation.gui.model.simulation.BasicSensors.OnOffSensor
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.GradientFXDrawer.doubleToColor
import it.unibo.scafi.simulation.gui.view.scalaFX.nodeToAbsolutePosition


object FastFXDrawer extends FXDrawer {


  override type OUTPUTNODE = javafx.scene.shape.Shape
  override def nodeGraphicsNode[INPUTNODE <: World#NODE](node: INPUTNODE): OUTPUTNODE = nodeToShape.create(node)

  override def deviceToGraphicsNode[INPUTDEV <: World#DEVICE](node: OUTPUTNODE, dev: INPUTDEV, lastValue: Option[OUTPUTNODE]): Option[OUTPUTNODE] = {
    import scalafx.Includes._
    val nodeWidth = node.boundsInLocal().getWidth
    var currentR = nodeWidth
    val point = nodeToAbsolutePosition(node)
    if(!lastValue.isDefined) {
      dev match {
        case OnOffSensor(value) => {
          val shape = node.asInstanceOf[Shape]
          if(value){
            val color = StandardFXDrawer.colors(dev.name.toString)
            if(gsensor.name == dev.name && shape.fillProperty().value == Color.BLACK) {
              node.fillProperty().setValue(color)
            } else if(gsensor.name != dev.name) {
              node.fillProperty().setValue(color)
            }
          } else {
            if(jfxPaint2sfx(shape.fillProperty().value) == StandardFXDrawer.colors(dev.name.toString)) {
              node.fillProperty().setValue(Color.BLACK)
            }
          }
        }
        case _ => None
      }
    }
    None
  }
}
