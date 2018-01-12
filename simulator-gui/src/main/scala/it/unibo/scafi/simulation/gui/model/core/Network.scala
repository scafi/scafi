package it.unibo.scafi.simulation.gui.model.core

/**
  * an interface of an immutable network of nodes connected
  */
trait Network {
  self : World =>
  type T <: Topology

  val topology : T
  /**
    * the neighbours of a node
    * @param n the node
    * @return a set of neighbours
    */
  def neighbours(n: NODE): Set[NODE]

  /**
    * the neighbour in the world
    * @return
    */
  def neighbours(): Map[NODE,Set[NODE]]
}
//STRATEGY
/**
  * define a topology of the network
  */
trait Topology {
  /**
    * declare if this node could be accept in the network or not
    * @param node the node
    * @param neighbour the neighbour
    * @return true if the neighbour is added to the node false otherwise
    */
  def acceptNeighbour(node : Node, neighbour : Node) : Boolean
}
