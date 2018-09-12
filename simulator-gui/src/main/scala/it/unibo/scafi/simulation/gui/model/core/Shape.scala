package it.unibo.scafi.simulation.gui.model.core

import it.unibo.scafi.simulation.gui.model.space.Point

/**
  * define a generic shape
  */
trait Shape {
  /**
    * the orientation type
    */
  type O
  /**
    * @return the orientation of the shape
    *
    */
  def orientation : O

  /**
    * check if a generic point is contained by the shape
    * @param p the point
    * @return true is if contained false otherwise
    */
  def contains(p : Point) : Boolean
}

