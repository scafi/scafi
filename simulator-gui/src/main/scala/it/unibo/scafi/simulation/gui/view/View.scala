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
trait SimulationView[W <: World] extends View{
  /**
    * out a set of node that are added or moved
    * @param node the nodes
    */
  def outNode(node : W#NODE)

  /**
    * remove a node into the output
    * @param node the node
    */
  def removeNode(node : W#ID)

  /**
    * out a neighbour of a node
    */
  def outNeighbour(nodes : (W#ID,Set[W#ID]))

  /**
    * remove a set of neighbour of a node
    */
  def removeNeighbour(nodes : (W#ID,Set[W#ID]))

  /**
    * output the device associated to the node
    * @param node the node
    */
  def outDevice(node : W#NODE)

  /**
    * remove all devices associated to a node
    * @param node the node
    */
  def clearDevice(node : W#ID)

  /**
    * apply the changes declared
    */
  def flush()
}
