/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object DemoLauncher extends Launcher {
  Settings.Sim_ProgramClass = "sims.Timer"
  Settings.ShowConfigPanel = true
  Settings.Sim_NbrRadius = 0.15
  Settings.Sim_NumNodes = 100
  parseCommandLine()
  launch()
}
