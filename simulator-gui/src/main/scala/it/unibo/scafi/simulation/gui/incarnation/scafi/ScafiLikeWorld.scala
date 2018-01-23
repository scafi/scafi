package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.simulation.BasicPlatform
import it.unibo.scafi.simulation.gui.model.space.Point3D

trait ScafiLikeWorld extends BasicPlatform{
  self : ScafiLikeWorld.Dependency =>
  override type ID = Int
  override type NAME = String
  override type P = Point3D
}

object ScafiLikeWorld {
  type Dependency = AggregateWorld.Dependency
}
