package it.unibo.scafi.simulation.gui.incarnation.scafi.world

/**
  * an implementation of scafi like world
  * with cartesian metric and
  * with no boundary (the boundary can
  * be update at runtime)
  */
object scafiWorld extends ScafiLikeWorld {
  override type B = Boundary
  override type M = Metric
  override val metric: M = this.cartesianMetric
  var boundary: Option[B] = None
}