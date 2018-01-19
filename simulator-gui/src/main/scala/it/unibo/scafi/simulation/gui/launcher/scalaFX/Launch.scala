package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.view.scalaFX.SimulationWindow

import scalafx.application.Platform

object Launch extends App {
  new JFXPanel()

  Platform.runLater {

    val dialogStage = new SimulationWindow {

      title = this.name
    }

    // Show dialog and wait till it is closed
    dialogStage.showAndWait

    // Force application exit
    System.exit(0)
  }

}
