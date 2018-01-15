package it.unibo.scafi.simulation.gui.model.common.network

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.model.core.{Network, Node}
import it.unibo.scafi.simulation.gui.pattern.observer.Event

/**
  * a network mutable. produce event when the network change
  */
trait ObservableNetwork extends Network {
  this : ObservableWorld =>
  protected val emptyNeighbours : Map[NODE,Set[NODE]] = nodes.map(x => x -> Set[NODE]()) toMap

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
  def clearNeighbours(n : NODE) : Unit = {
    _neighbours += n -> Set[NODE]()
    this !!! ObservableNetwork.nodeNeighboursCleared(n)

  }
  /**
    * remove a set of neighbour of a node
    * @param n the node
    * @param neighbour the neighbour of the node
    * @return true if the node is present false otherwise
    */
  def removeNeighbours(n : NODE, neighbour : Set[NODE]) : Boolean = {
    if(!((_neighbours get n) isDefined)) return false
    val currentNeighbour = _neighbours(n)

    val newNeighbour = currentNeighbour -- neighbour
    _neighbours += n -> newNeighbour
    this !!! ObservableNetwork.nodeNeighboursRemoved(n,neighbour.asInstanceOf[Set[Node]])
    true
  }
  /**
  //TODO
  /**
    * add a neighbour to the current node
    * @param node the node
    * @param neighbour the new neighbour
    */
  def addNeighbour(node: NODE, neighbour: NODE) = {

  }

  def addNeighbours(node : NODE, neighbour: NODE)
  **/
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
