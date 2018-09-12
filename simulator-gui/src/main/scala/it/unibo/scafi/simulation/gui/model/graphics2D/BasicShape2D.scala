package it.unibo.scafi.simulation.gui.model.graphics2D

import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}

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
        case Point2D(x,y) => check(x, y)
        case Point3D(x,y,_) => check(x, y)
      }
    }
  }

  /**
    * @param r the radius of the circle
    */
  case class Circle(r : Float, override val orientation : Float = 0) extends Shape2D {
    override def contains(p: Point): Boolean = {
      def check(x : Double, y : Double) : Boolean = math.sqrt((x-r) * (x-r) + (y-r) * (y-r)) <= r
      p match {
        case Point2D(x,y) => check(x,y)
        case Point3D(x,y,_) => check(x,y)
      }
    }
  }

  /**
    * describe a generic Polygon
    * @param orientation the orientation of the polygon
    * @param points the point in the space (in order) of the polygon
    */
  case class Polygon(override val orientation: Float ,points: Point2D *) extends Shape2D {
    //TODO FAST SOLUTION,
    import javafx.scene.shape.{Polygon => JFXPolygon}
    private val internalPoly = new JFXPolygon(points.flatMap {x => List(x.x,x.y)}:_*)
    override def contains(p: Point): Boolean = p match {
      case Point2D(x,y) => internalPoly.contains(x,y)
      case Point3D(x,y,_) => internalPoly.contains(x,y)
    }

  }

}

