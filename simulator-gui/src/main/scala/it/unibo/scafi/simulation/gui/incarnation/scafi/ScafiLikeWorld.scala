package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.model.aggregate.implementation.immutable.AggregateWorld
import it.unibo.scafi.simulation.gui.model.common.{BoundaryDefinition, MetricDefinition}
import it.unibo.scafi.simulation.gui.model.sensor.implementation.mutable.SensorDefinition
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.{SensorPlatform, StandardNetwork, StandardNodeDefinition, World3D}
import it.unibo.scafi.simulation.gui.model.space.Point3D

/**
  * a world describe a plaftform like scafi
  */
trait ScafiLikeWorld extends SensorPlatform with World3D {
  self : ScafiLikeWorld.Dependency =>
  override type ID = Int
  override type NAME = String
  override type P = Point3D
}

object ScafiLikeWorld {
  trait SensorType

  object in extends SensorType
  object out extends SensorType
  type Dependency = AggregateWorld.Dependency
}

/**
  * an incarnation to a scafi like world
  */
object ScafiWorld extends ScafiLikeWorld
  with SensorDefinition
  with StandardNodeDefinition
  with StandardNetwork
  with BoundaryDefinition
  with MetricDefinition {

  override type B = Boundary

  override type M = Metric

  override val metric: M = cartesinMetric

  /**
    * A boundary of the world (a world may has no boundary)
    */
  var boundary: Option[B] = None
}