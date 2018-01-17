package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesChangeEvent
import it.unibo.scafi.simulation.gui.model.core.Node

object AggregateEvent {

  /**
    * the root of all aggregate event
    * @tparam N the type of node influenced
    */
  sealed trait AggregateEvent[N <: Node] extends NodesChangeEvent[N]

  /**
    * an event produced when a node is moved in the world
    * @param nodes the node moved
    * @tparam N the type of node influenced
    */
  case class NodesMoved[N <: Node](nodes : Set[N]) extends AggregateEvent[N]

  /**
    * an event produced a generic device change in a set of node
    * @param nodes the node
    * @tparam N the type of node influenced
    */
  case class NodesDeviceChanged[N <: Node](nodes: Set[N]) extends AggregateEvent[N]
}
