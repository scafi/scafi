package it.unibo.scafi.simulation.gui.model

import it.unibo.scafi.simulation.gui.Simulation

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
trait SimulationManager {
  var simulation: Simulation

  def setPauseFire(pauseFire: Double)

  def start()

  def resume()

  def stop()

  def pause()

  def step(num_step: Int)
}