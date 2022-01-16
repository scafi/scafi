package it.unibo.scafi.space.graphics2D

import it.unibo.scafi.space.Point2D
import it.unibo.scafi.space.Point3D

/**
 * define some basic operation
 */
object BasicShape2D {
  /**
   * @param h
   *   the height of the rectangle
   * @param w
   *   the width of the rectangle
   * @param orientation
   *   the orientation of the rectangle
   */
  case class Rectangle(w: Float, h: Float, orientation: Float = 0) extends Shape2D {
    override def contains(p: Point3D): Boolean = p.x >= 0 && p.y >= 0 && p.x <= w && p.y <= h
  }

  /**
   * @param r
   *   the radius of the circle
   */
  case class Circle(r: Float, override val orientation: Float = 0) extends Shape2D {
    override def contains(p: Point3D): Boolean = math.sqrt((p.x - r) * (p.x - r) + (p.y - r) * (p.y - r)) <= r
  }

  /**
   * describe a generic Polygon
   * @param orientation
   *   the orientation of the polygon
   * @param points
   *   the point in the space (in order) of the polygon
   */
  case class Polygon(override val orientation: Float, points: Point2D*) extends Shape2D {
    import javafx.scene.shape.{Polygon => JFXPolygon}
    private val internalPoly = new JFXPolygon(points.flatMap(x => List(x.x, x.y)): _*)
    override def contains(p: Point3D): Boolean = internalPoly.contains(p.x, p.y)
  }

}
