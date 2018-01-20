package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * define a platform for simple simulation
  */
trait BasicPlatform extends AggregateWorld{
  self : BasicPlatform.Dependency =>

  override type M = CartesianMetric.type

  override val metric = CartesianMetric

  override val boundary = None
  object CartesianMetric extends Metric {
    override def positionAllowed(p: P): Boolean = true
  }
}

object BasicPlatform {
  type Dependency = AggregateWorld.Dependency
}
