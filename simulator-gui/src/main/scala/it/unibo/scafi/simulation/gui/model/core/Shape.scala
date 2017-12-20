package it.unibo.scafi.simulation.gui.model.core

/**
  * define a generic shape
  */
trait Shape {
  /**
    * the type of dimension
    */
  type DIMENSION;

  /**
    *
    * @return the dimension of the shape
    */
  def dimension : DIMENSION

  /**
    *
    * @return the scale of the shape
    */
  def scale : Int
}

