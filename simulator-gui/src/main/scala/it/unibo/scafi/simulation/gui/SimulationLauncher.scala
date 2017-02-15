package it.unibo.scafi.simulation.gui
import it.unibo.scafi.simulation.gui.controller.Controller


/**
  * Created by mirko on 2/9/17.
  */
class SimulationLauncher extends App {
    def parseCommandLine(): Unit = SimulationCmdLine.parse(args, Settings)
    def launch(): Unit = Controller.startup
}

object SimulationLauncher extends SimulationLauncher{
    parseCommandLine()
    launch()
}
