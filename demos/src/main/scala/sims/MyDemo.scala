package sims

import it.unibo.scafi.simulation.gui._
import it.unibo.scafi.simulation.gui.model._

object MyDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.MyDemo" // starting class, via Reflection
  Settings.ShowConfigPanel = false          // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15             // neighbourhood radius
  Settings.Sim_NumNodes = 100               // number of nodes
  launch()
}

class MyDemo extends AggregateProgram {
  override def main() = rep(0)(_+1)         // the aggregate program to run
}