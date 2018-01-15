package it.unibo.scafi.simulation.gui.model.common.aggregate

import it.unibo.scafi.simulation.gui.model.common.node.NodeFactory
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld

trait AggregateWorld extends ObservableWorld {
  type FACTORY <: NodeFactory[NODE]
}