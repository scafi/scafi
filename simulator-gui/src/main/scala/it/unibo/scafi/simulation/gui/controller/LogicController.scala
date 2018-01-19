package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld.WorldObserver
import it.unibo.scafi.simulation.gui.model.simulation.SimulationPlatform
import it.unibo.scafi.simulation.gui.view.SimulationOutput

trait LogicController[W <: SimulationPlatform] extends WorldController[W] {
  self : WorldObserver[W#NODE] =>
  type OUTPUT <: SimulationOutput
}
