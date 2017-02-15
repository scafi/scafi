package sims

import it.unibo.scafi.simulation.gui.{Settings, SimulationLauncher}

/**
  * Created by mirko on 2/15/17.
  */
object ExamplelLauncher extends SimulationLauncher {
  Settings.Sim_ProgramClass = "sims."
  Settings.ShowConfigPanel = true
  Settings.Sim_Sensors = ""
  parseCommandLine()
  launch()
}
