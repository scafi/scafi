package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer

import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.s2.frontend.view.ViewSetting._
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX._
import javafx.scene.control.Label
import javafx.scene.text.Font
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Ellipse

/**
 * standard policy used to show nodes. node are showed using javafx shapes using standard mapping. led device are showed
 * like ellipse with some color other device are showed with a label
 */
case object StandardFXOutput extends FXOutputPolicy {
  // the type of output is Node because string device are showed like label
  override type OUTPUT_NODE = javafx.scene.Node
  // radius used to show boolean device
  private val radius = 2
  // allow to show multiple label
  private var labelOut = Map.empty[OUTPUT_NODE, List[OUTPUT_NODE]]
  // used standard conversion of model shape to scalafx shape
  override def nodeGraphicsNode(node: NODE): OUTPUT_NODE = modelShapeToFXShape.apply(node.shape, node.position)
  // used to draw device
  override def deviceToGraphicsNode(node: OUTPUT_NODE, dev: DEVICE): Option[OUTPUT_NODE] = drawNode(dev, node)

  override def updateDevice(node: OUTPUT_NODE, dev: DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit =
    graphicsDevice match {
      case Some(graphics) =>
        dev match {
          case SensorDevice(sens) =>
            sens.value[Any] match {
              // if device was a led sensor i can the state of visibility (true is visible false is hide)
              case led: Boolean => graphics.setVisible(led)
              // if it is numeric this strategy update the label text value
              case numeric: Double => graphics.asInstanceOf[Label].setText(numeric.toInt.toString)
              // in other case this strategy update the label text value
              case v => graphics.asInstanceOf[Label].setText(v.toString)
            }
          // if the device isn't a sensor, this strategy do nothing
          case _ =>
        }
      case None => // if there isn't no device out before I can't change the state of device
    }

  private def drawNode(dev: DEVICE, node: OUTPUT_NODE): Option[OUTPUT_NODE] = {
    import scalafx.Includes._
    // take the absolute position of node (without node traslation)
    val point = nodeToAbsolutePosition(node)
    dev match {
      case SensorDevice(sens) =>
        sens.value[Any] match {
          // if the device is a led i create a circle with the color selected into view setting
          case led: Boolean =>
            val index = deviceName.indexOf(dev.name.toString)
            val middleBound = node.boundsInLocal.value.getWidth / 2
            val res: Node = new Ellipse {
              this.centerX.bind(node.translateX + point.x)
              this.centerY.bind(node.translateY + point.y)
              // i use plus +1 to render the first value of device name
              this.radiusX = index * radius + middleBound + 1
              this.radiusY = index * radius + middleBound + 1
              this.strokeWidth = radius
              val color: Color = deviceColor(index % deviceColor.size)
              this.stroke = color
              this.fill = Color.Transparent
              this.visible = led
              this.smooth = false
            }
            Some(res)

          case v =>
            // list tell me how many label is render on the scene, this value allow to show label one under the other
            val list: List[OUTPUT_NODE] = labelOut.get(node) match {
              case None => List.empty[OUTPUT_NODE]
              case Some(list: List[OUTPUT_NODE]) => list.filter(x => x != null).filter(x => x.isVisible)
            }
            val label = new Label(v.toString)
            val labelOffset = node.getBoundsInLocal.getWidth / 2 + labelFontSize / 2
            label.font = new Font(labelFont, labelFontSize)
            label.layoutX.bind(node.translateX + point.x)
            label.layoutY.bind(node.translateY + point.y + labelOffset / 4 + (list.size) * labelOffset)
            labelOut += node -> (label :: list)
            Some(label)
        }
      case _ => None
    }
  }
}
