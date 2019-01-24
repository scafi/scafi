package it.unibo.scafi.simulation.frontend.model

trait Network {
  def nodes: Map[Int,Node]

  def neighbourhood: Map[Node, Set[Node]]

  def setNodeNeighbours(id: Int, newNeighbours: Iterable[Int])

  def setNeighbours(value: Map[Int, Iterable[Int]]): Unit

  def neighbourhoodPolicy: NbrPolicy

  def observableValue: Set[String]
}
