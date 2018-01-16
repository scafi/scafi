package it.unibo.scafi.simulation.gui.model.common.network

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.model.core.{Network, Node}
import it.unibo.scafi.simulation.gui.pattern.observer.Event

/**
  * a network mutable. produce event when the network change
  */
//TODO PENSA BENE COME EVITARE SITUAZIONI CRITICHE DOVUTE ALLA CANCELLAZIONE DEI NODI
trait ObservableNetwork extends Network {
  this : ObservableWorld =>
  protected def emptyNeighbours : Map[NODE,Set[NODE]] = nodes.map(x => x -> Set[NODE]()) toMap

  private var _neighbours : Map[NODE,Set[NODE]] = emptyNeighbours
  /**
    * the neighbour in the world
    * @return
    */

  override def neighbours(n: NODE): Set[NODE] = {
    val res = _neighbours get n
    if(res isEmpty) return Set()
    return res get
  }

  override def neighbours(): Map[NODE, Set[NODE]] = _neighbours
  /**
    * remove all neighbours in the network
    */
  def clearNeighbours() : Unit = {
    _neighbours = emptyNeighbours
    this !!! ObservableNetwork.networkNeighboursCleared()
  }
  /**
    * remove neighbour of one node in the network
    * @param n the node
    */
  def clearNeighbours(n : NODE) : Boolean = {
    if(!nodes.contains(n)) return false
    _neighbours += n -> Set[NODE]()
    this !!! ObservableNetwork.nodeNeighboursCleared(n)
    true
  }
  /**
    * remove a set of neighbour of a node
    * @param n the node
    * @param neighbour the neighbour of the node
    * @return true if the node is present in the world false otherwise
    */
  def removeNeighbours(n : NODE, neighbour : Set[NODE]) : Boolean = {
    if(!this.nodes.contains(n)) return false
    if(!((_neighbours get n) isDefined)) {
      this._neighbours += n -> Set[NODE]()
    }
    val currentNeighbour = _neighbours(n)

    val newNeighbour = currentNeighbour -- neighbour
    _neighbours += n -> newNeighbour
    this !!! ObservableNetwork.nodeNeighboursRemoved(n,neighbour.asInstanceOf[Set[Node]])
    true
  }

  /**
    * add a set of neighbour in a node
    * @param node the node
    * @param neighbours the neighbour to add
    * @throws IllegalArgumentException if the node isn't in the world
    * @return a set on node that could't be neighbour of the node
    */
  def addNeighbours(node : NODE, neighbours: Set[NODE]) : Set[NODE] = {
    checkNodeInTheWorkd(node,neighbours)
    if((!((_neighbours get node)).isDefined)) _neighbours += node -> Set[NODE]()
    val currentNeighbour = _neighbours(node)
    val filterNodes = this.neighboursAllowed(node,neighbours)
    val newNeighbour = currentNeighbour ++ filterNodes
    _neighbours += node -> newNeighbour
    return neighbours -- filterNodes
  }

  /**
    * replace the current neighbour of a node
    * @param node the node
    * @param neighbour the new neigbour
    * @return a set on node that could't be neighbour of the node passed
    */
  def replaceNeighbours(node : NODE, neighbour: Set[NODE]) : Set[NODE] = {
    checkNodeInTheWorkd(node,neighbour)
    this.clearNeighbours(node)
    return this.addNeighbours(node,neighbour)
  }
  private def checkNodeInTheWorkd(node : NODE, neighbour: Set[NODE]): Unit = {
    require(this.nodes.contains(node))
    require(neighbour.forall(this.nodes.contains(_)))
  }
  protected def neighboursAllowed(node : NODE, nodes : Set[NODE]) : Set[NODE] = {
    nodes.filter(this.topology.acceptNeighbour(node,_))
  }
}

object ObservableNetwork {
  /**
    * an event used to tell that the network is cleared
    */
  case class networkNeighboursCleared() extends Event

  /**
    * an event used to tell that the neighbours of some node are cleared
    * @param n the node
    */
  case class nodeNeighboursCleared(n : Node) extends Event

  /**
    * an event used to tell that a subset of neighbours of some node is removed
    * @param n the node
    * @param neighbours the old neighbours removed
    */
  case class nodeNeighboursRemoved(n : Node, neighbours : Set[Node]) extends Event
}
