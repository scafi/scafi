package it.unibo.scafi.simulation.gui.model.graphics2D

import it.unibo.scafi.simulation.gui.model.core.Shape
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}

/**
  * the root of all shape2d
  */
trait Shape2D extends Shape{
  override type O = Float

  def contains(p : Point) : Boolean
}

object Shape2D {
  val BASIC_ORIENTATION = 0
}
