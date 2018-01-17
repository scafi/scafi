package it.unibo.scafi.simulation.gui.model.common.network

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.model.core.Network
import it.unibo.scafi.simulation.gui.pattern.observer.Source

/**
  * a network mutable. produce event when the network change
  */
trait ObservableNetwork extends Network {
  this : ObservableNetwork.Dependency =>
  override type NODE <: ObservableNetwork.Dependency#NODE
  import CommonNetworkEvent._
  /**
    * the neighbours of a node
    * @param n the node
    * @return a set of neighbours
    */
  override def neighbours(n: NODE): Set[NODE] = {
    val res = this.neighbours().get(n)
    if(res isEmpty) return Set()
    return res get
  }
  /**
    * remove all neighbours in the network
    */
  def clearNeighbours() : Unit = {
    this.neighbours().foreach(y => removeStrategy(y._1))
    this !!! networkNeighboursCleared()
  }
  /**
    * remove neighbour of one node in the network
    * @param n the node
    */
  def clearNeighbours(n : NODE) : Boolean = {
    if(this.apply(n.id.asInstanceOf[NODE#ID]).isEmpty) return false
    removeStrategy(n)
    this !!! nodeNeighboursCleared(n)
    true
  }
  /**
    * remove a set of neighbour of a node
    * @param n the node
    * @param neighbour the neighbour of the node
    * @return true if the node is present in the world false otherwise
    */
  def removeNeighbours(n : NODE, neighbour : Set[NODE]) : Boolean = {
    checkNodeInTheWorld(n,neighbour)
    if(!((this.neighbours().get(n)) isDefined)) {
     addStrategy(n,Set[NODE]())
    }
    val currentNeighbour = neighbours(n)

    val newNeighbour = currentNeighbour -- neighbour
    addStrategy(n,newNeighbour)
    this !!! nodeNeighboursRemoved(n,neighbour)
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
    checkNodeInTheWorld(node,neighbours)
    if((!((this.neighbours().get(node))).isDefined)) addStrategy(node,Set[NODE]())
    val currentNeighbour = this.neighbours(node)
    val filterNodes = neighboursAllowed(node,neighbours)
    val newNeighbour = neighbours
    addStrategy(node,newNeighbour)
    return neighbours -- filterNodes
  }

  /**
    * replace the current neighbour of a node
    * @param node the node
    * @param neighbour the new neigbour
    * @return a set on node that could't be neighbour of the node passed
    */
  def replaceNeighbours(node : NODE, neighbour: Set[NODE]) : Set[NODE] = {
    checkNodeInTheWorld(node,neighbour)
    this.clearNeighbours(node)
    return this.addNeighbours(node,neighbour)
  }
  private def checkNodeInTheWorld(node : NODE, neighbour: Set[NODE]): Unit = {
    require(this.apply(node.id.asInstanceOf[NODE#ID]).isDefined,"node isn't in the network")
    require(neighbour.forall(x => this.apply(x.id.asInstanceOf[NODE#ID]).isDefined),"illegal neighbour")
  }
  protected def neighboursAllowed(node : NODE, nodes : Set[NODE]) : Set[NODE] = {
    nodes.filter(this.topology.acceptNeighbour(node,_))
  }

  //TEMPLATE METHOD
  /**
    * define how to insert a set on node in the neighbour
    * @param node the node
    * @param nodes the neighbours
    */
  protected def addStrategy(node : NODE, nodes : Set[NODE])
  //TEMPLATE METHOD
  /**
    * define how to remove a node in the neighbours
    * @param node the node
    */
  protected def removeStrategy(node : NODE)
}
object ObservableNetwork {
  type Dependency = ObservableWorld with Source
}
