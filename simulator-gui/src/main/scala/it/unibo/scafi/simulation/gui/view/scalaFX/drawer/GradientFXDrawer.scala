package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import javafx.scene.Node
import javafx.scene.shape.Shape

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.simulation.BasicSensors.DisplaySensor.DoubleSensor
import it.unibo.scafi.simulation.gui.view.scalaFX.nodeToAbsolutePosition

import scalafx.scene.control.Label
import scalafx.scene.paint.Color

object GradientFXDrawer extends FXDrawer {
  var maxValue = 1000.0
  val maxColor = 255.0
  override type OUTPUTNODE = javafx.scene.shape.Shape
  override def deviceToGraphicsNode[INPUTDEV <: World#DEVICE](node: OUTPUTNODE, dev: INPUTDEV, lastValue: Option[OUTPUTNODE]): Option[OUTPUTNODE] = {
    //the offset to the label of the y coordinate
    import scalafx.Includes._
    val nodeWidth = node.boundsInLocal().getWidth
    var currentR = nodeWidth
    val point = nodeToAbsolutePosition(node)
    if(!lastValue.isDefined) {
      dev match {
        case DoubleSensor(value) => {
          node.fillProperty().setValue(doubleToColor(value))
        }
        case _ => None
      }
    }
    None
  }

  private implicit def doubleToColor(v : Double) : Color  = {
    if(v > maxValue) {
      Color.Black
    } else {
      val color = ((maxColor / maxValue) * v - 255) * -1;
      Color.rgb(color.toInt,0,0);
    }

  }
  override def nodeGraphicsNode[INPUTNODE <: World#NODE](node: INPUTNODE): OUTPUTNODE = nodeToShape.create(node)
}
