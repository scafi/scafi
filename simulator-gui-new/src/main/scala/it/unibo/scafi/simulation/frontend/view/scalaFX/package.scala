package it.unibo.scafi.simulation.frontend.view



import javafx.embed.swing.JFXPanel
import it.unibo.scafi.space.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.space.Point3D

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
package object scalaFX {
  /**
    * convert a point in a point2D in javafx
    * @param p the point
    * @return the fx point
    */
  implicit def pointToScalaFXPoint(p : Point3D) : FXPoint = new FXPoint(p.x,p.y)

  /**
    * get the position of node
    * @param n the node
    * @return the position in the scene
    */
  def nodeToPosition(n : Node) : FXPoint = {
    val node = nodeToAbsolutePosition(n)
    new FXPoint(node.x + n.translateX.value, node.y + n.translateY.value)
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
  def initializeScalaFXPlatform(): Unit = {
    new JFXPanel
  }
  /**
    * a rich representation of fx node that can be cloned
    * @param node the node
    */
  implicit class RichNode(node : Node) {
    import javafx.scene.shape.{Circle, Rectangle}

    import scalafx.Includes._
    val standardNode = new Rectangle(2,2)
    def cloneNode() : Node = {
      node.delegate match {
        case rect : Rectangle =>
          val rectCloned = new Rectangle(rect.x.value + rect.translateX.value,
            rect.y.value + rect.translateY.value,
            rect.width.value,
            rect.height.value)
          rectCloned.fill = rect.fill()
          rectCloned
        case circle : Circle =>
          val circleCloned = new Circle(circle.centerX.value + circle.translateX.value,
            circle.centerY.value + circle.translateY.value,
            circle.radius.value)
          circleCloned.fill = circle.fill()
          circleCloned
        case _ =>
          standardNode.x = nodeToAbsolutePosition(node).x + node.translateX.value
          standardNode.y = nodeToAbsolutePosition(node).y + node.translateY.value
          standardNode
      }
    }
  }
}
