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
  def outNode(node : Set[W#NODE])

  /**
    * remove a node into the output
    * @param node the node
    */
  def removeNode(node : Set[W#ID])

  /**
    * out a neighbour of a node
    */
  def outNeighbour(nodes : Map[W#NODE,Set[W#NODE]])

  /**
    * remove a set of neighbour of a node
    */
  def removeNeighbour(nodes : Map[W#ID,Set[W#ID]])

  /**
    * output the device associated to the node
    * @param node the node
    */
  def outDevice(node : Set[W#NODE])

  /**
    * remove all devices associated to a node
    * @param node the node
    */
  def clearDevice(node : Set[W#ID])
}
