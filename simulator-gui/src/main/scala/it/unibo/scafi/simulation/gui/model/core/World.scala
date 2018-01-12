package it.unibo.scafi.simulation.gui.model.core

import it.unibo.scafi.simulation.gui.model.space.Position

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
    * The metric of this world
    */
  val metric : Metric
  /**
    * A boundary of the world (a world can has no boundary)
    */
  val boundary : Option[Boundary]
  /**
    * get all nodes on this world
    */
  def nodes :Set[NODE]

  /**
    * return a specific node on the world
    * @param id
    *   the id of the node
    * @return
    *   the node if it is in the world
    */
  def apply(id : NODE#ID) : Option[NODE]
}
//STRATEGY
/**
  * a generic boundary
  */
trait Boundary {
  def nodeAllowed(n : Node) : Boolean
}
//STRATEGY
/**
  * a generic metric
  */
trait Metric {
  def positionAllowed(p : Position) : Boolean
}