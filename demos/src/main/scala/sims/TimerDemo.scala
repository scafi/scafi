/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, ScafiStandardLibraries}
import ScafiStandardLibraries._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object TimerDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.SimpleTimer" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

class SimpleTimer extends ScafiStandardAggregateProgram with SensorDefinitions with BlockT {
  override def main() = branch(sense1){timer(100)}{0}
}
