package it.unibo.scafi.simulation.gui.model


/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
trait Network {
  def nodes: Map[Int,Node]

  def neighbourhood: Map[Node, Set[Node]]

  def neighbourhoodPolicy: NbrPolicy

  def observableValue: Set[String]
}