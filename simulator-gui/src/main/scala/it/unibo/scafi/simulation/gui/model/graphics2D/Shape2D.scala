package it.unibo.scafi.simulation.gui.model.graphics2D

import it.unibo.scafi.simulation.gui.model.core.Shape

/**
  * the root of all shape2d
  */
trait Shape2D extends Shape{
  override type O = Float
}

object Shape2D {
  val BASIC_ORIENTATION = 0
}
