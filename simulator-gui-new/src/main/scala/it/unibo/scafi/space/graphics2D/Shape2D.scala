package it.unibo.scafi.space.graphics2D

import it.unibo.scafi.space.Shape

/**
 * the root of all shape2d
 */
trait Shape2D extends Shape {
  override type O = Float
}

object Shape2D {
  val BASIC_ORIENTATION = 0
}
