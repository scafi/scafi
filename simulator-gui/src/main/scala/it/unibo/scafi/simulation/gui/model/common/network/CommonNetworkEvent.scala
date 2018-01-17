package it.unibo.scafi.simulation.gui.model.common.network

import it.unibo.scafi.simulation.gui.model.core.Node
import it.unibo.scafi.simulation.gui.pattern.observer.Event

object CommonNetworkEvent {
  /**
    * an event used to tell that the network is cleared
    */
  case class networkNeighboursCleared() extends Event

  /**
    * a event produced when a neighbour of a node is cleared
    * @param n the node
    * @tparam N the type of node
    */
  case class nodeNeighboursCleared[N <: Node](n : N) extends Event

  /**
    * an event used to tell that a subset of neighbours of some node is removed
    * @param n the node
    * @param neighbours the old neighbours removed
    * @tparam N the type of node
    */
  case class nodeNeighboursRemoved[N <: Node](n : N, neighbours : Set[N]) extends Event

  /**
    * an event produced when a subset of neighbours of some some is added
    * @param n the node
    * @param neighbours the neighbours
    * @tparam N the type of node
    */
  case class nodeNeighboursAdded[N <: Node](n : N, neighbours : Set[N]) extends Event
}
