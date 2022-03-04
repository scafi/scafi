/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.AggregateProgram
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object BasicDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.BasicProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

class BasicProgram extends AggregateProgram {
  override def main(): Int = rep(0)(_ + 1) // the aggregate program to run
}
