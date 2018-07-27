package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import javafx.scene.control.Label

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.gui.view.scalaFX._

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Ellipse

object StandardFXOutputPolicy extends FXOutputPolicy {
  //TODO create a non static color map
  override type OUTPUTNODE = javafx.scene.Node
  private val maxTextLength = 200
  val colors: Map[String, Color] = Map(sensor1 -> Color.Red,
    sensor2 -> Color.Yellow,
    sensor3 -> Color.Blue,
    output1 -> Color.LimeGreen)
  val size: Map[String, Double] = Map(sensor1 -> 1, sensor2 -> 3, sensor3 -> 5, output1 -> 7)
  val radius = 2

  override def nodeGraphicsNode(node: NODE): OUTPUTNODE = nodeToShape.create(node)

  override def deviceToGraphicsNode (node: OUTPUTNODE, dev: DEVICE): Option[OUTPUTNODE] = drawNode(dev,node)

  override def updateDevice(node : OUTPUTNODE, dev: DEVICE, graphicsDevice: Option[OUTPUTNODE]): Unit = {
    if(graphicsDevice.isEmpty) return
    val graphics = graphicsDevice.get
    dev match {
      case SensorDevice(sens) => sens.value match {
        case led : Boolean => graphics.setVisible(led)
        case numeric : Double => graphics.asInstanceOf[Label].setText(numeric.toInt.toString)
        case v => graphics.asInstanceOf[Label].setText(v.toString)
      }
      case _ =>
    }
  }

  private def drawNode (dev: DEVICE, node: OUTPUTNODE): Option[OUTPUTNODE] = {
    import scalafx.Includes._
    val point = nodeToAbsolutePosition(node)
    dev match {
      case SensorDevice(sens) => sens.value match {
        case led : Boolean => {
          val res : Node = new Ellipse {
            this.centerX.bind(node.translateX + point.x)
            this.centerY.bind(node.translateY + point.y)
            this.radiusX = size(dev.name.toString)
            this.radiusY = size(dev.name.toString)
            this.strokeWidth = radius
            val color: Color = colors(dev.name.toString)
            this.stroke = color
            this.fill = Color.Transparent
            this.visible = led
            this.smooth = false
          }
          Some(res)
        }
        case v => {
          val label = new Label(v.toString)
          label.setMaxWidth(maxTextLength)
          label.layoutX.bind(node.translateX + point.x)
          label.layoutY.bind(node.translateY + point.y)
          Some(label)
        }
      }
      case _ => None
    }
  }
}
