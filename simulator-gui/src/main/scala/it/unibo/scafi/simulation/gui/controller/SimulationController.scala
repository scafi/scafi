package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateNode
import it.unibo.scafi.simulation.gui.view.SimulationOutput

trait SimulationController[N <: AggregateNode] extends WorldController[N] {
  type O <: SimulationOutput
}
