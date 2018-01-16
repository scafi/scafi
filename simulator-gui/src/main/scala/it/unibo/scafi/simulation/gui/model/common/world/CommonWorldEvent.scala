package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.Node
import it.unibo.scafi.simulation.gui.pattern.observer.Event
//TODO RICORDATI DI DOCUMENTARE
object CommonWorldEvent {
  trait SingleNodeChange[N <: Node] extends Event { def node : N }

  object SingleNodeChange {
    def unapply[N <: Node](arg: SingleNodeChange[N]): Option[N] = Some(arg.node)
  }

  trait MultipleNodeChange[N <: Node] extends Event { def nodes : Set[N]}

  object MultipleNodeChange {
    def unapply[N <: Node](arg: MultipleNodeChange[N]): Option[Set[N]] = Some(arg.nodes)
  }
  case class NodeAdded[N <: Node](node : N) extends SingleNodeChange[N]

  case class NodeRemoved[N <: Node](node : N) extends SingleNodeChange[N]

  case class NodesAdded[N <: Node](nodes : Set[N]) extends MultipleNodeChange[N]

  case class NodesRemoved[N <: Node](nodes : Set[N]) extends MultipleNodeChange[N]
}
