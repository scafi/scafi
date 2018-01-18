package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.{AggregateNode, AggregateWorld}
import it.unibo.scafi.simulation.gui.model.common.network.ObservableNetwork

/**
  * a trait used to describe a world with a network
  */
//TODO PENSA AD UNA STRUTTURA DATI MIGLIORE PER GESTIRE LA RETE
trait SimulationPlatform extends AggregateWorld with ObservableNetwork {
  self : SimulationPlatform.Dependency =>
  override type NODE <: AggregateNode
  private var _neighbours : Map[NODE,Set[NODE]] = Map[NODE,Set[NODE]]()

  override def neighbours(): Map[NODE, Set[NODE]] = _neighbours

  override protected def addStrategy(node: NODE, nodes: Set[NODE]): Unit = this._neighbours += node -> nodes

  override protected def removeStrategy(node: NODE): Unit = this._neighbours -= node

  override def insertNode (n:NODE) : Boolean = {
    val res = super.insertNode(n)
    if(!res) return false
    _neighbours += n -> Set()
    true
  }
  override def insertNodes (n : Set[NODE]): Set[NODE] = {
    val res = super.insertNodes(n)
    (n -- res) foreach {_neighbours += _ -> Set() }
    return n
  }

  override def removeNode(n:NODE#ID) : Boolean = {
    val node = this.apply(n)
    val res = super.removeNode(n)
    if(!res) return false
    val toRemove = this._neighbours.get(node.get).get
    removing(node.get,toRemove)
    true
  }
  override def removeNodes(n:Set[NODE#ID]) : Set[NODE] = {
    val nodes = this.apply(n)
    val notRemoved = super.removeNodes(n)
    (nodes -- notRemoved) foreach { x => removing(x,this._neighbours.get(x).get)}
    return notRemoved
  }

  private def removing(n: NODE, ns : Set[NODE]) = {
    ns foreach { x => _neighbours += x -> (_neighbours(x) - n)}
    _neighbours -= n
  }
}

object SimulationPlatform {
  type Dependency = AggregateWorld.Dependency with ObservableNetwork.Dependency
}
