package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.view.SimulationOutput

/**
  * a controller that has a defined logic to change
  * the world
 *
  * @tparam W the world observed
  */
trait LogicController[W <: AggregateWorld] extends WorldController[W] {
  type OUTPUT <: SimulationOutput
}
