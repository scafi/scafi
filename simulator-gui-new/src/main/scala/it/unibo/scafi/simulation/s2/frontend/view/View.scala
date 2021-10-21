package it.unibo.scafi.simulation.s2.frontend.view

import it.unibo.scafi.simulation.s2.frontend.model.core.World
import it.unibo.scafi.space.{Point3D, Shape}

/**
  * root trait of view concept
  */
trait View

trait Container[OUTPUT <: View] {
  /**
    * @return the current output in container
    */
  def output : OUTPUT

  /**
    * try to render the output
    * @return true if the output is rendered false otherwise
    */
  def render() : Unit
}
/**
  * describe a generic output of a simulation
  */
trait SimulationView extends View {
  type NODE = World#Node
  type ID = Any
  type NAME = Any
  type DEVICE = World#Device
  /**
    * out a set of node that are added or moved
    * @param node the nodes
    */
  def outNode(node : NODE): Unit

  /**
    * remove a node into the output
    * @param node the node
    */
  def removeNode(node : ID): Unit

  /**
    * out a neighbour of a node
    */
  def outNeighbour(nodes : (ID,Set[_ <: ID])): Unit

  /**
    * remove a set of neighbour of a node
    */
  def removeNeighbour(nodes : (ID,Set[_ <: ID])): Unit

  /**
    * output the device associated to the node
    * @param node the node
    */
  def outDevice(node : ID, device : DEVICE): Unit

  /**
    * remove device associated to a node
    * @param node the node
    */
  def clearDevice(node : ID, name : NAME): Unit

  /**
    * set the boundary in the simulation view
    * @param boundary world boundary
    */
  def boundary_=(boundary : Shape): Unit

  /**
    * set wall inside map
    * @param walls the world walls
    */
  def walls_=(walls : Seq[(Shape, Point3D)]): Unit
  /**
    * apply the changes declared
    */
  def flush(): Unit
}
