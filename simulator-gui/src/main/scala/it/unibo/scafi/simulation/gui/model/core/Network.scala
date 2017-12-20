package it.unibo.scafi.simulation.gui.model.core

/**
  * an interface of an immutable network of nodes connected
  */
trait Network {
  type NODE <: Node

  type TOPOLOGY <: Topology

  /**
    * the neighbours of a node
    * @param n the node
    * @return a set of neighbours
    */
  def neighbours(n: Node): Set[Node]

  /**
    * @return all node in this network
    */
  def nodes(): Set[Node]

  /**
    * generic topology of the network
    * **/
  trait Topology
}

class RandomTopology extends Network#Topology
