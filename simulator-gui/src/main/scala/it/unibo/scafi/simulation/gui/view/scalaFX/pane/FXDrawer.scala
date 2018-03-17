package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.simulation.BasicSensors.{OnOffSensor, TextSensor}
import it.unibo.scafi.simulation.gui.view.Drawer
import it.unibo.scafi.simulation.gui.view.scalaFX._

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Ellipse, Rectangle}
class FXDrawer extends Drawer {

  override type OUTPUTNODE = javafx.scene.Node

  override def nodeGraphicsNode[INPUTNODE <: World#NODE](node: INPUTNODE): OUTPUTNODE = {
    val p: FXPoint = node.position
    val defaultShape = new Rectangle {
      this.x = p.x
      this.y = p.y
      this.width = 10
      this.height = 10
      this.fill = Color.Red
    }
    var shape : Node = defaultShape
    if(node.shape.isDefined) {
      node.shape.get match {
        case r : InternalRectangle => shape = new Rectangle {
          this.x = p.x
          this.y = p.y
          this.width = r.w
          this.height = r.h
        }
        case c : InternalCircle => shape = new Circle {
          this.centerX = p.x
          this.centerY = p.y
          this.radius = c.r
          this.smooth = false
        }
        case _ =>
      }
    }
    shape
  }

  override def deviceToGraphicsNode[INPUTDEV <: World#DEVICE](devs: Set[INPUTDEV], node : OUTPUTNODE): Set[OUTPUTNODE] = {
    //the offset to the label of the y coordinate
    import scalafx.Includes._
    val yoffset = 5
    val label = new Label
    val nodeWidth = node.boundsInLocal().getWidth
    val increaseRadius = node.boundsInLocal.value.getWidth
    val lineWidth = 2
    var currentR = nodeWidth
    val point = nodeToAbsolutePosition(node)
    label.layoutX.bind(node.translateX +point.x)
    label.layoutY.bind(node.translateY + point.y + yoffset)
    var res : Set[javafx.scene.Node] = Set(label)
    devs foreach  { device =>
      device match {
        case TextSensor(value) => {
          if(!value.isEmpty) {
            label.text = label.text.value + "" + device.name.toString + " = " + value
          }
        }
        case OnOffSensor(value) => {
          if(value) {
            val ellipse = new Ellipse{
              this.centerX.bind(node.translateX + point.x)
              this.centerY.bind(node.translateY + point.y)
              this.radiusX = currentR
              this.radiusY = currentR
              this.strokeWidth = lineWidth
              val color : Color = colors(device.name.toString)
              this.stroke = color
              this.fill = Color.color(color.red,color.green,color.blue,0.5)
              this.smooth = false
            }
            res += ellipse
          }
          currentR += increaseRadius
        }
        case _ => {
          None
        }
      }
    }
    res
  }
}
