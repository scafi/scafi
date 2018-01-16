package it.unibo.scafi.simulation.gui.model.core

import it.unibo.scafi.simulation.gui.model.space.{Point}

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
  type B <: Boundary[NODE]
  /**
    * the type of metric in this world
    */
  type M <: Metric[NODE#P]
  /**
    * The metric of this world
    */
  val metric : M
  /**
    * A boundary of the world (a world may has no boundary)
    */
  val boundary : Option[B]
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

  /**
    * return a set of node in the world
    * @param nodes the ids of the node
    * @return the set of the node
    */
  def apply(nodes : Set[NODE#ID]) : Set[NODE]
}
//STRATEGY
/**
  * a generic boundary
  */
trait Boundary[N <: Node] {
  def nodeAllowed(n : N) : Boolean
}
//STRATEGY
/**
  * a generic metric
  */
trait Metric[P <: Point] {
  def positionAllowed(p : P) : Boolean
}