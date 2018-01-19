package it.unibo.scafi.simulation.gui.view.scalaFX


import it.unibo.scafi.simulation.gui.view.{GraphicsOutput, Window}

import scalafx.stage.Stage
//TODO
class SimulationWindow extends Stage with Window {
  override type OUTPUT = GraphicsOutput

  override def name: String = "Simulation pane"

  override def close: Unit = this.close()

  override def output: Set[OUTPUT] = ???
}
