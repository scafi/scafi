package it.unibo.scafi.simulation.gui.model.core

/**
  * define a generic shape
  */
trait Shape {
  type O
  /**
    * @return the orientation of the shape
    *
    */
  def orientation : O
}

