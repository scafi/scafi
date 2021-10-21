package it.unibo.scafi.simulation.frontend.model

import it.unibo.scafi.simulation.frontend.Simulation

trait SimulationManager {
  var simulation: Simulation

  def setPauseFire(pauseFire: Double): Unit

  def setUpdateNodeFunction(updateNodeValue: Int => Unit): Unit

  def start(): Unit

  def resume(): Unit

  def stop(): Unit

  def pause(): Unit

  def step(num_step: Int): Unit
}
