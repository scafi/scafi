package it.unibo.scafi.simulation.gui.view



import javafx.application.Application
import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.model.core.{Shape => InternalShape}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}
import it.unibo.scafi.simulation.gui.view.WindowConfiguration.{FullScreen, Window}

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.stage.Screen
package object scalaFX {
  //color of neighbour line
  val lineColor = Color(0.8,0.8,0.8,0.2)

  /**
    * convert a point in a point2D in javafx
    * @param p the point
    * @return the fx point
    */
  implicit def pointToScalaFXPoint(p : Point) : FXPoint = p match {
    case p: Point3D => new FXPoint(p.x,p.y)
    case p : Point2D => new FXPoint(p.x,p.y)
  }

  /**
    * get the position of node
    * @param n the node
    * @return the position in the scene
    */
  def nodeToPosition(n : Node) : FXPoint = {
    val node = nodeToAbsolutePosition(n)
    return new FXPoint(node.x + n.translateX.value, node.y + n.translateY.value)
  }

  /**
    * get the absolute position of node (a position that don't compute node translation)
    * @param n the node
    * @return absolute position
    */
  def nodeToAbsolutePosition(n : Node) : FXPoint = {
    val bounds = n.boundsInLocal.value
    val middleWidth = bounds.getWidth / 2
    val middleHeight = bounds.getHeight / 2
    new FXPoint(bounds.getMinX + middleWidth, bounds.getMinY + middleHeight)
  }

  /**
    * convert fx point into model point
    * @param p the point to convert
    * @return node converted
    */
  def pointToWorldPosition(p : FXPoint) : Point3D = Point3D(p.x,p.y,0)

  /**
    * initialize fx environment
    */
  def initializeScalaFXPlatform (): Unit = {
    new JFXPanel
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)
  }
  /**
    * a rich representation of fx node that can be cloned
    * @param node the clonable node
    */
  implicit class RichNode(node : Node) {
    import javafx.scene.shape.{Circle, Rectangle}

    import scalafx.Includes._
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
