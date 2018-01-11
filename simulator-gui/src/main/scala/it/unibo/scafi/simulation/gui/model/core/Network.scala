package it.unibo.scafi.simulation.gui.model.core

/**
  * an interface of an immutable network of nodes connected
  */
trait Network {
  self : World =>
  /**
    * the neighbours of a node
    * @param n the node
    * @return a set of neighbours
    */
  def neighbours(n: Node): Set[Node]

  trait Topology
}

/**
  * A random topology
  */
class RandomTopology extends Network#Topology
