package it.unibo.scafi.simulation.gui.model

import it.unibo.scafi.simulation.gui.Simulation

trait SimulationManager {
  var simulation: Simulation

  def setPauseFire(pauseFire: Double)

  def start()

  def resume()

  def stop()

  def pause()

  def step(num_step: Int)
}
