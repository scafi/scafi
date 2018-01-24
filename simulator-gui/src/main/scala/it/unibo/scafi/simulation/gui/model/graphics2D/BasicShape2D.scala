package it.unibo.scafi.simulation.gui.model.graphics2D

import it.unibo.scafi.simulation.gui.model.space.Point2D

/**
  * define some basic shape
  */
object BasicShape2D {

  /**
    * @param h the height of the rectangle
    * @param w the width of the rectangle
    * @param orientation the orientation of the rectangle
    */
  case class Rectangle(h : Float, w : Float, orientation : Float = 0) extends Shape2D

  /**
    * @param r the radius of the circle
    */
  case class Circle(r : Float, override val orientation : Float = 0) extends Shape2D

  /**
    * describe a generic Polygon
    * @param orientation the orientation of the polygon
    * @param points the point in the space (in order) of the polygon
    */
  case class Polygon(orientation: Float, points: Point2D *) extends Shape2D
}
