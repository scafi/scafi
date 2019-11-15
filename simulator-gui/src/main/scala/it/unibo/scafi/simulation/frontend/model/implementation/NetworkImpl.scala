/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.model.implementation

import it.unibo.scafi.simulation.frontend.model.{NbrPolicy, Network, Node}

class NetworkImpl(var nodes: Map[Int,Node], var neighbourhoodPolicy: NbrPolicy) extends Network {
  var neighbours = Map[Node, Set[Node]]()

  def neighbourhood: Map[Node, Set[Node]] = {
    neighbours
  }

  def observableValue: Set[String] = {
    Set[String]("Neighbours",
      "Id",
      "Export",
      "A sensor",
      "None")
  }

  def setNodeNeighbours(id: Int, nbrs: Iterable[Int]): Unit ={
    var currentNode = nodes(id)
    var nbrsSet = nbrs.iterator.map(nodes(_)).toSet

    currentNode.removeAllNeghbours()
    currentNode.addAllNeighbours(nbrsSet)

    neighbours += (currentNode -> nbrsSet)

    neighbours.keys.foreach(node => {
      var neighbourNbrs = neighbours(node)
      if(nbrsSet.contains(node)){
        if(!neighbourNbrs.contains(currentNode)){
          neighbours += node -> (neighbourNbrs + currentNode)
        }
      } else {
        if(neighbourNbrs.contains(currentNode)){
          neighbours += node -> (neighbourNbrs - currentNode)
        }
      }
    })
  }

  def setNeighbours(nbrMap: Map[Int, Iterable[Int]]): Unit  = {
    var newNeighbours = nbrMap.map {
      case (id: Int, nbrs: Iterable[Int]) => nodes(id) -> nbrs.iterator.map(nodes(_)).toSet
    }
    nodes.values.foreach(node => {
      node.removeAllNeghbours()
      node.addAllNeighbours(newNeighbours(node))
    })
    this.neighbours = newNeighbours
  }
}
