package it.unibo.scafi.simulation.gui.incarnation.scafi.world
import it.unibo.scafi.space.SpatialAbstraction

/**
  * an implementation of scafi like world
  * with cartesian metric and
  * with no boundary (the boundary can
  * be update at runtime)
  */
object scafiWorld extends ScafiLikeWorld {
 var boundary: Option[SpatialAbstraction.Bound] = None
}