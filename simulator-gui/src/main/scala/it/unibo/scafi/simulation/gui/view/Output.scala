package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.Node

trait Output

trait SimulationOutput {
  def out(node : Node)

  def out(node : Set[Node])
}

