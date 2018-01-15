package it.unibo.scafi.simulation.gui.model.graphics2D

/**
  * define some basic shape
  */
object BasicShape2D {

  /**
    *
    * @param x the coordinate of upper left corner of the rectangle
    * @param y the coordinate of upper left corner of the rectangle
    * @param h the height of the rectangle
    * @param w the width of the rectangle
    * @param orientation the orientation of the rectangle
    */
  case class Rectangle(x: Float, y : Float, h : Float, w : Float, orientation : Float) extends Shape2D

  /**
    *
    * @param x the coordinate of the center of circle
    * @param y the coordinate of the center of circle
    * @param r the radius of the circle
    * @param orientation the orientation of the shape
    */
  case class Circle(x: Float, y : Float, r : Float, orientation : Float) extends Shape2D

  /**
    * describe a generic Polygon
    * @param orientation the orientation of the polygon
    * @param points the point in the space (in order) of the polygon
    */
  case class Polygon(orientation: Float, points: (Float,Float) *) extends Shape2D
}
