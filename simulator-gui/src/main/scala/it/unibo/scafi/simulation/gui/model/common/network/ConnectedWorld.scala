package it.unibo.scafi.simulation.gui.model.common.network

import it.unibo.scafi.simulation.gui.model.core.World

/**
  * a world with a network
  */
trait ConnectedWorld {
  this : World =>
  /**
    * the type of network
    */
  type NET <: Network
  trait Network {
    /**
      * the neighbours of a node
      * @param n the node
      * @return a set of neighbours
      */
    def neighbours(n: ID): Set[ID]

    /**
      * the neighbour in the world
      * @return the network
      */
    def neighbours(): Map[ID,Set[ID]]

    /**
      * set a neighbours of a node
      * @param node the node in thw world
      * @param neighbour the neighbour
      */
    def setNeighbours(node :ID,neighbour :Set[ID])
  }

  def network : NET
}
