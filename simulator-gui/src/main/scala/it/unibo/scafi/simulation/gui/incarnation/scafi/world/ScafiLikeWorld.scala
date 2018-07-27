package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.command.WorldCommandSpace
import it.unibo.scafi.simulation.gui.model.common.world.WorldDefinition.World3D
import it.unibo.scafi.simulation.gui.model.common.{BoundaryDefinition, MetricDefinition}
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.model.simulation.implementation.StandardNetwork
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.{SensorDefinition, StandardNodeDefinition}
import it.unibo.scafi.simulation.gui.model.space.Point3D

/**
  * a world describe a scafi platform
  */
trait ScafiLikeWorld extends SensorPlatform with World3D with SensorDefinition with StandardNodeDefinition {
  override type ID = Int
  override type NAME = String
  override type P = Point3D
}

object ScafiLikeWorld {
  object scafiWorldCommandSpace extends WorldCommandSpace[ScafiLikeWorld] {
    override val world: ScafiLikeWorld = scafiWorld
  }
}

/**
  * an incarnation to a scafi world
  */
object scafiWorld extends ScafiLikeWorld
  with StandardNetwork
  with BoundaryDefinition
  with MetricDefinition {

  override type B = Boundary

  override type M = Metric

  override val metric: M = cartesinMetric

  var boundary: Option[B] = None
}