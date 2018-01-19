package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.Node

trait Output

trait Container {
  type OUTPUT <: Output

  def output : Set[OUTPUT]
}

trait SimulationOutput extends Output{
  def out(node : Set[Node])

  def remove(node : Set[Node])

  def outNeighbour(node : Node, neighbour : Set[Node])
}
