package it.unibo.scafi.simulation.gui.view



import scalafx.application.Platform

import it.unibo.scafi.simulation.gui.model.core.{Shape => InternalShape}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
//TODO THINK WHERE ADD COLOR (SIMPLE TEST)
//TODO FOR PERFORMANCE USE IMAGE INSTEAD OF SHAPE -> TO DRAW IMAGE JAVAFX DON'T USE CPU , ONLY GPU
package object scalaFX {
  object RichPlatform {
    def thenRunLater(graphics: => Any)(after: => Any): Unit = {
      Platform.runLater{
        graphics
        after
      }
    }
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
  def nodeToWorldPosition(n: Node, start : FXPoint) : Point3D = {
    val defaultZ = 0
    Point3D(start.x + n.translateX.value, start.y + n.translateY.value, defaultZ)
  }

}
