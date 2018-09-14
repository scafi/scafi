package it.unibo.scafi.simulation.gui.model.common

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.space.{Point3D, Shape}

/**
  * definition of standard boundary
  * used by world to verify the correctness
  * of node position with their shape
  */
trait BoundaryDefinition {
  self: World =>
  /**
    * @return shape representation of world bound
    */
  def worldBound : Option[Shape] = this.boundary match {
    case Some(_) => Some(this.boundary.get.inclusive)
    case _ => None
  }

  /**
    * @return shape representation of wold walls
    */
  def worldWalls : Seq[(Shape,Point3D)] = this.boundary match {
    case Some(_) => this.boundary.get.exclusive
    case _ => Seq.empty
  }
}
