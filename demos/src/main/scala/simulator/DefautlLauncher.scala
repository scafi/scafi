package simulator

import it.unibo.scafi.simulation.gui._
import it.unibo.scafi.simulation.gui.controller.Controller

/**
  * Created by mirko on 2/9/17.
  */
object DefautlLauncher extends App {
    Settings.Sim_ProgramClass = "sims."
    Settings.ShowConfigPanel = true
    Settings.Sim_Sensors = ""
    SimulationCmdLine.parse(args, Settings)
    Controller.startup
}
