package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world
import it.unibo.scafi.space.SpatialAbstraction

/**
 * an implementation of scafi like world with no boundary (the boundary can be update at runtime)
 */
object scafiWorld extends ScafiLikeWorld {
  var boundary: Option[SpatialAbstraction.Bound] = None
}
