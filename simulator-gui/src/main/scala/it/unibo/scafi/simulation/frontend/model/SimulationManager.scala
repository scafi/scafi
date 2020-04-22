package it.unibo.scafi.simulation.frontend.model

import it.unibo.scafi.simulation.frontend.Simulation

trait SimulationManager {
  var simulation: Simulation

  def setPauseFire(pauseFire: Double)

  def setUpdateNodeFunction(updateNodeValue: Int => Unit): Unit

  def start()

  def resume()

  def stop()

  def pause()

  def step(num_step: Int)
}
