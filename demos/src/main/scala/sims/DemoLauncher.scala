package sims

import it.unibo.scafi.simulation.gui._

object DemoLauncher extends Launcher{
  Settings.Sim_ProgramClass = "sims."
  Settings.ShowConfigPanel = true
  Settings.Sim_NbrRadius = 0.15
  Settings.Sim_NumNodes = 100
  parseCommandLine()
  launch()
}
