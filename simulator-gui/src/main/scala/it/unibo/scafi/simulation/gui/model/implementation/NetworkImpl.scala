package it.unibo.scafi.simulation.gui.model.implementation


import it.unibo.scafi.simulation.gui.model.{Network, Node}

/**
  * Created by Varini on 14/11/16.
  * Converted to Scala by Casadei on 3/02/17
  */
class NetworkImpl(val nodes: Map[Int,Node], var neighbourhoodPolicy: Any) extends Network {
  calculateNeighbours

  def neighbourhood: Map[Node, Set[Node]] = {
    return calculateNeighbours
  }

  def observableValue: Set[String] = {
    Set[String]("Neighbours",
      "Id",
      "Export",
      "A sensor",
      "None")
  }

  private def calculateNeighbours: Map[Node, Set[Node]] = {
    var neighbours = Set[Node]()
    var res = Map[Node, Set[Node]]()
    var nbrRadius: Double = 0.2 // Default
    if (neighbourhoodPolicy.isInstanceOf[Double]) {
      nbrRadius = neighbourhoodPolicy.asInstanceOf[Double]
    }
    for (n <- nodes.values) {
      neighbours = Set()
      n.removeAllNeghbours
      for (n1 <- nodes.values) {
        val distance: Double = Math.hypot(n.position.getX - n1.position.getX, n.position.getY - n1.position.getY)
        if (distance <= nbrRadius) {
          neighbours += n1
        }
      }
      n.addAllNeighbours(neighbours)
      res += n -> neighbours
    }
    return res
  }
}