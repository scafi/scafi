package it.unibo.scafi.simulation.gui.model.simulation.implementation

import it.unibo.scafi.simulation.gui.model.aggregate.{AbstractAggregateWorld, AbstractNodeDefinition}
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld
import it.unibo.scafi.simulation.gui.model.sensor.{SensorConcept, SensorNetwork, SensorWorld}
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.SensorDefinition
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

import scala.collection.mutable.{Map => MMap}
/**
  * a standard network definition
  */
trait StandardNetwork  {
  self: SensorPlatform =>

  override type NET = Network

  val network : NET = new NetworkImpl

  private class NetworkImpl extends Network {
    private var neigh : MMap[ID,Set[ID]] = MMap.empty
    /**
      * the neighbours of a node
      *
      * @param n the node
      * @return a set of neighbours
      */
    override def neighbours(n: ID): Set[ID] = neigh.getOrElse(n,Set())

    /**
      * the neighbour in the world
      *
      * @return the network
      */
    override def neighbours(): Map[ID, Set[ID]] = neigh toMap

    /**
      * set a neighbours of a node
      *
      * @param node      the node in thw world
      * @param neighbour the neighbour
      */
    override def setNeighbours(node: ID, neighbour: Set[ID]): Unit = neigh += node -> neighbour
  }

}