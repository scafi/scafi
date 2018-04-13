package it.unibo.scafi.simulation.gui.model.graphics2D

import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}
import it.unibo.scafi.simulation.gui.model.space.Point._

/**
  * define some basic operation
  */
object BasicShape2D {
  /**
    * @param h the height of the rectangle
    * @param w the width of the rectangle
    * @param orientation the orientation of the rectangle
    */
  case class Rectangle(w : Float, h : Float, orientation : Float = 0) extends Shape2D {
    override def contains(p: Point): Boolean = {
      def check(x : Double, y : Double) : Boolean = x >= 0 && y >= 0 && x <= w && y <= h
      p match {
        case p2d : Point2D => check(p2d.x,p2d.y)

        case p3d : Point3D => check(p3d.x, p3d.y)
      }
    }
  }

  /**
    * @param r the radius of the circle
    */
  case class Circle(r : Float, override val orientation : Float = 0) extends Shape2D {
    //TODO
    override def contains(p: Point): Boolean = ???
  }

  /**
    * describe a generic Polygon
    * @param orientation the orientation of the polygon
    * @param points the point in the space (in order) of the polygon
    */
  case class Polygon(orientation: Float, points: Point2D *) extends Shape2D {
    //TODO
    override def contains(p: Point): Boolean = ???
  }

}

