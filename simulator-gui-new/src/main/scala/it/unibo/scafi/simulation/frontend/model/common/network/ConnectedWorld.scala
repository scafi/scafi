package it.unibo.scafi.simulation.frontend.model.common.network

import it.unibo.scafi.simulation.frontend.model.common.world.CommonWorldEvent.EventType
import it.unibo.scafi.simulation.frontend.model.core.World

/**
  * a world with a network
  */
trait ConnectedWorld {
  this : ConnectedWorld.Dependency =>
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
  /**
    return the current representation of the network
   */
  def network : NET
}
object ConnectedWorld {
  type Dependency = World

  /**
    * event produced when the node neighbour change
    */
  object NeighbourChanged extends EventType
}
