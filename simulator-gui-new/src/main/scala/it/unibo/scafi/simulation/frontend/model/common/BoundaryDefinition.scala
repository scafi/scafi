package it.unibo.scafi.simulation.frontend.model.common

import it.unibo.scafi.simulation.frontend.model.core.World
import it.unibo.scafi.space.{Point3D, Shape}

/**
  * definition of standard boundary
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
