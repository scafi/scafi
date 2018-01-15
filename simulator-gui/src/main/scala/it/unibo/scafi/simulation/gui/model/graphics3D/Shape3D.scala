package it.unibo.scafi.simulation.gui.model.graphics3D

import it.unibo.scafi.simulation.gui.model.core.Shape

trait Shape3D extends Shape{
  override type O = (Float,Float,Float)
}

object Shape3D {
  val BASIC_ORIENTATION = (0,0,0)
}