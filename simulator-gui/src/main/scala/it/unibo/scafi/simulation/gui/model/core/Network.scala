package it.unibo.scafi.simulation.gui.model.core

/**
  * an interface of an immutable network of nodes connected
  */
trait Network {
  self : World =>
  type T <: Topology

  def topology : T
  /**
    * the neighbours of a node
    * @param n the node
    * @return a set of neighbours
    */
  def neighbours(n: Node): Set[Node]
}

/**
  * A random topology
  */

trait Topology {

}
