package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.model.common.{BoundaryDefinition, MetricDefinition}
import it.unibo.scafi.simulation.gui.model.simulation.implementation.StandardNetwork

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