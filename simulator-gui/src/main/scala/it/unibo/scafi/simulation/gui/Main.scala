package it.unibo.scafi.simulation.gui

import javax.swing._

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.implementation.SimulationManagerImpl
import it.unibo.scafi.simulation.gui.view.{ConfigurationPanel, SimulatorUI}

/**
  * Created by chiara on 19/10/16.
  */
object Main extends App {
  SimulationCmdLine.parse(args, Settings)

  SwingUtilities.invokeLater(new Runnable() {
    def run() {
      Controller.getIstance.setGui(new SimulatorUI)
      Controller.getIstance.setSimManager(new SimulationManagerImpl)
      if(Settings.ShowConfigPanel) new ConfigurationPanel
      else Controller.getIstance.startSimulation()
    }
  })
}