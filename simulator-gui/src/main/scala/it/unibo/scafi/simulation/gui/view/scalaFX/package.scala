package it.unibo.scafi.simulation.gui.view



import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.model.core.{Shape => InternalShape}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Rectangle}
package object scalaFX {
  val lineColor = Color(0.8,0.8,0.8,0.2)

  /**
    * convert a point in a point2D in javafx
    * @param p the point
    * @return the fx point
    */
  implicit def PointToScalaFXPoint(p : Point) : FXPoint = p match {
    case p: Point3D => new FXPoint(p.x,p.y)
    case p : Point2D => new FXPoint(p.x,p.y)
  }

  def nodeTranslationToPosition(n : Node, p: FXPoint) : FXPoint = new FXPoint(n.translateX.value + p.x, n.translateY.value + p.y)

  def nodeToAbsolutePosition(n : Node) : FXPoint = {
    val bounds = n.boundsInLocal.value
    val middleWidth = bounds.getWidth / 2
    val middleHeight = bounds.getHeight / 2
    new FXPoint(bounds.getMinX + middleWidth, bounds.getMinY + middleHeight)
  }

  def nodeToWorldPosition(n: Node, start : FXPoint) : Point3D = {
    val defaultZ = 0
    Point3D(start.x + n.translateX.value, start.y + n.translateY.value, defaultZ)
  }

  def initializeScalaFXPlatform (): Unit = {
    new JFXPanel
  }

  implicit class RichNode(node : Node) {
    import scalafx.Includes._
    import javafx.scene.shape.Rectangle
    import javafx.scene.shape.Circle
    val standardNode = new Rectangle(2,2)
    def cloneNode() : Node = {
      node.delegate match {
        case rect : Rectangle => {
          new Rectangle(rect.x.value + rect.translateX.value,
            rect.y.value + rect.translateY.value,
            rect.width.value,
            rect.height.value)
        }
        case circle : Circle => {
          new Circle(circle.centerX.value + circle.translateX.value,
            circle.centerY.value + circle.translateY.value,
            circle.radius.value)
        }
        case _ => {
          standardNode.x = nodeToAbsolutePosition(node).x + node.translateX.value
          standardNode.y = nodeToAbsolutePosition(node).y + node.translateY.value
          standardNode
        }
      }
    }
  }
}
