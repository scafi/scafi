package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.{World, Shape => InternalShape}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Rectangle}

package object scalaFX {
  /**
    * create a fx node by a Node
    * @param node the node of the world
    * @tparam N the type of node
    * @return the node created
    */
  def nodeToScalaFXNode[N <: World#Node](node: N) : Node = {
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
          this.fill = Color.Red
        }
        case c : InternalCircle => shape = new Circle {
          this.centerX = p.x
          this.centerY = p.y
          this.radius = c.r
        }
        case _ =>
      }
    }
    shape
  }

  /**
    * convert a point in a point2D in javafx
    * @param p the point
    * @return the fx point
    */
  implicit def PointToScalaFXPoint(p : Point) : FXPoint = {
    p match {
      case p: Point3D => new FXPoint(p.x,p.y)
      case p : Point2D => new FXPoint(p.x,p.y)
    }
  }

  def nodeToAbsolutePosition(n : Node) : FXPoint = {
    val bounds = n.boundsInLocal.value
    val middleWidth = bounds.getWidth / 2
    val middleHeight = bounds.getHeight / 2
    new FXPoint(bounds.getMinX + middleWidth, bounds.getMinY + middleHeight)
  }
  def bindNodes(source : Node, observer : Node) = {

  }
}
