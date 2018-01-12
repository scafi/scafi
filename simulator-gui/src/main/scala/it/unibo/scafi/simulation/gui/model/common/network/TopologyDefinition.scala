package it.unibo.scafi.simulation.gui.model.common.network

import it.unibo.scafi.simulation.gui.model.core.{Node, Topology}

/**
  * basic topology definition
  */
object TopologyDefinition {
  object RandomTopology extends Topology {
    /**
      * accept all neighbour
      */
    override def acceptNeighbour(node: Node, neighbour: Node): Boolean = true
  }
}
