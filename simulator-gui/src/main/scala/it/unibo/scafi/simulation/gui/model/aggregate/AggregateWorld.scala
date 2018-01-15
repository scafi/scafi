package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld

trait AggregateWorld extends ObservableWorld {
  override type NODE <: AggregateNode
}