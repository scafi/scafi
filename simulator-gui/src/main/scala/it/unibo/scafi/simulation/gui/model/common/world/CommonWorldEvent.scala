package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.Node
import it.unibo.scafi.simulation.gui.pattern.observer.Event

/**
  * define a set of basic event produced by observable world
  */
object CommonWorldEvent {
  /**
    * a generic event that influence a set of node
    * @tparam N the type of node influenced
    */
  trait NodesChangeEvent[N <: Node] extends Event { def nodes : Set[N]}

  /**
    * used to pattern matching (extractor)
    */
  object NodesChangeEvent {
    def unapply[N <: Node](arg: NodesChangeEvent[N]): Option[Set[N]] = Some(arg.nodes)
  }

  /**
    * produced when a set of node is added in the world
    * @param nodes the nodes
    * @tparam N the type of node influenced
    */
  case class NodesAdded[N <: Node](nodes : Set[N]) extends NodesChangeEvent[N]

  /**
    * produced when a set of node is removed in the world
    * @param nodes the nodes
    * @tparam N the type of node influenced
    */
  case class NodesRemoved[N <: Node](nodes : Set[N]) extends NodesChangeEvent[N]
}
