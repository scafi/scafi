package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.World

trait View

trait Container {
  type OUTPUT <: View

  def output : Set[OUTPUT]
}
/**
  * describe a generic output of a simulation
  */
trait SimulationView extends View {
  type NODE = World#Node
  type ID = Any
  /**
    * out a set of node that are added or moved
    * @param node the nodes
    */
  def outNode(node : NODE)

  /**
    * remove a node into the output
    * @param node the node
    */
  def removeNode(node : ID)

  /**
    * out a neighbour of a node
    */
  def outNeighbour(nodes : (ID,Set[_ <: ID]))

  /**
    * remove a set of neighbour of a node
    */
  def removeNeighbour(nodes : (ID,Set[_ <: ID]))

  /**
    * output the device associated to the node
    * @param node the node
    */
  def outDevice(node : NODE)

  /**
    * remove all devices associated to a node
    * @param node the node
    */
  def clearDevice(node : ID)

  /**
    * apply the changes declared
    */
  def flush()
}
