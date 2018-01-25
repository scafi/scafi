package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.World

trait Output

trait Container {
  type OUTPUT <: Output

  def output : Set[OUTPUT]
}

/**
  * describe a generic output of a simulation
  */
trait SimulationOutput extends Output{
  /**
    * out a set of node that are added or moved
    * @param node the nodes
    * @tparam N the type of nodes
    */
  def outNode[N<: World#Node] (node : Set[N])

  /**
    * remove a node into the output
    * @param node the node
    * @tparam ID the id of node
    */
  def removeNode[ID <: World#ID](node : Set[ID])

  /**
    * out a neighbour of a node
    * @param node the node
    * @param neighbour his neighbours
    * @tparam N the type of node
    */
  def outNeighbour[N <: World#Node] (node : N, neighbour : Set[N])

  /**
    * remove a set of neighbour of a node
    * @param node the node
    * @param neighbour his neighbour
    * @tparam ID the id of node
    */
  def removeNeighbour[ID <: World#ID](node : ID, neighbour : Set[ID])
}
