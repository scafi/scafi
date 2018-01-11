package it.unibo.scafi.simulation.gui.model.core

/**
  * describe a place where an immutable set of node are located
  */
trait World {
  /**
    * the type of node in this world
    */
  type NODE <: Node
  /**
    * the type of boundary of the world
    */
  type B <: Boundary
  /**
    * the type of metric in this world
    */
  type M <: Metric
  /**
    * a generic boundary
    */
  trait Boundary

  /**
    * a generic metric
     */
  trait Metric
  /**
    * get all nodes on this world
    */
  def nodes :Set[NODE]

  /**
    * return a specific node on the world
    * @param id
    *   the id of the node
    * @return
    *   the node
    */
  def apply(id : NODE#ID) : NODE
}
