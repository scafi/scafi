package simulator

import it.unibo.scafi.simulation.gui._
import it.unibo.scafi.simulation.gui.controller.Controller

/**
  * Created by mirko on 2/9/17.
  */
object DefautlLauncher extends App {
    Settings.Sim_ProgramClass = "sims.Gradient"
    Settings.ShowConfigPanel = false
    SimulationCmdLine.parse(args, Settings)
    Controller.startup
}
