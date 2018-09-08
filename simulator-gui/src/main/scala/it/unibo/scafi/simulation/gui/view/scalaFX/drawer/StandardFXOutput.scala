package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import javafx.scene.control.Label

import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.gui.view.ViewSetting._
import it.unibo.scafi.simulation.gui.view.scalaFX._

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Ellipse

/**
  * standard policy used to show nodes
  */
case object StandardFXOutput extends FXOutputPolicy {
  override type OUTPUT_NODE = javafx.scene.Node
  val radius = 2

  override def nodeGraphicsNode(node: NODE): OUTPUT_NODE = modelShapeToFXShape.apply(node.shape,node.position)

  override def deviceToGraphicsNode (node: OUTPUT_NODE, dev: DEVICE): Option[OUTPUT_NODE] = drawNode(dev,node)

  override def updateDevice(node : OUTPUT_NODE, dev: DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit = {
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

  private def drawNode (dev: DEVICE, node: OUTPUT_NODE): Option[OUTPUT_NODE] = {
    import scalafx.Includes._
    val point = nodeToAbsolutePosition(node)
    dev match {
      case SensorDevice(sens) => sens.value match {
        case led : Boolean => {
          val index = deviceName.indexOf(dev.name.toString)
          val res : Node = new Ellipse {
            this.centerX.bind(node.translateX + point.x)
            this.centerY.bind(node.translateY + point.y)
            this.radiusX = index * radius + 1
            this.radiusY = index * radius + 1
            this.strokeWidth = radius
            val color: Color = deviceColor(index % deviceColor.size)
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
