package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.{AggregateNode, AggregateWorld}
import it.unibo.scafi.simulation.gui.model.common.network.ObservableNetwork

trait SimulationPlatform extends AggregateWorld with ObservableNetwork {
  self : SimulationPlatform.Dependency =>
  override type NODE <: AggregateNode
  private var _neighbours : Map[NODE,Set[NODE]] = this.nodes.map(x => x -> Set[NODE]()).toMap

  override def neighbours(): Map[NODE, Set[NODE]] = _neighbours

  override protected def add(node: NODE, nodes: Set[NODE]): Unit = this._neighbours += node -> nodes

  override protected def remove(node: NODE): Unit = this._neighbours -= node

}

object SimulationPlatform {
  type Dependency = AggregateWorld.Dependency with ObservableNetwork.Dependency
}
