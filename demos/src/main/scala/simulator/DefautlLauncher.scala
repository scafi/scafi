package simulator

import it.unibo.scafi.simulation.gui._
import it.unibo.scafi.simulation.gui.controller.Controller

/**
  * Created by mirko on 2/9/17.
  */
object DefautlLauncher extends App {
    Settings.Sim_NumNodes = 100
    SimulationCmdLine.parse(args, Settings)
    Controller.startup
}
