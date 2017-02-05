package it.unibo.scafi.simulation

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.implementation.SimulationManagerImpl
import it.unibo.scafi.simulation.gui.view.{ConfigurationPanel, SimulatorUI}
import javax.swing._

/**
  * Created by chiara on 19/10/16.
  */
object Main extends App {
  SwingUtilities.invokeLater(new Runnable() {
    def run() {
      Controller.getIstance.setGui(new SimulatorUI)
      Controller.getIstance.setSimManager(new SimulationManagerImpl)
      new ConfigurationPanel // Shows the panel for creating a new simulation on startup
    }
  })
}