package it.unibo.scafi.simulation.gui.model.simulation.implementation

import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld.NeighbourChanged
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform

import scala.collection.mutable.{Map => MMap}
/**
  * a standard network definition
  */
trait StandardNetwork  {
  self: SensorPlatform =>

  override type NET = Network

  val network : NET = new NetworkImpl

  private class NetworkImpl extends Network {
    private var neigh : Map[ID,Set[ID]] = Map.empty

    override def neighbours(n: ID): Set[ID] = neigh.getOrElse(n,Set())

    override def neighbours(): Map[ID, Set[ID]] = neigh

    override def setNeighbours(node: ID, neighbour: Set[ID]): Unit = {
      neigh += node -> neighbour
      StandardNetwork.this.notify(StandardNetwork.this.NodeEvent(node,NeighbourChanged))
    }
  }

}