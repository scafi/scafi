package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.World

trait Output

trait Container {
  type OUTPUT <: Output

  def output : Set[OUTPUT]
}

trait SimulationOutput extends Output{
  def out[N<: World#Node] (node : Set[N])

  def remove[ID <: World#ID](node : Set[ID])

  def outNeighbour[N <: World#Node] (node : N, neighbour : Set[N])
}
