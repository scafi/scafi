package it.unibo.scafi.simulation.gui.model.core

import it.unibo.scafi.simulation.gui.model.space.Dimension

/**
  * define a generic shape
  */
trait Shape {
  /**
    * the type of dimension
    */
  type DIMENSION <: Dimension

  /**
    *
    * @return the dimension of the shape
    */
  def dimension : DIMENSION

}

