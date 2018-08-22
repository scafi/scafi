package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.gui.view.scalaFX.common.{FXSelectionArea, KeyboardManager}
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutputPolicy}
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.FXSimulationPane
import it.unibo.scafi.simulation.gui.view.{AbstractKeyboardManager, AbstractSelectionArea, Container, SimulationView}

import scalafx.application.Platform
import scalafx.scene.layout.HBox

/**
  * standard fx view eniromento
  */
object ScalaFXEnvironment extends ViewEnvironment[SimulationView]{

  var drawer : FXOutputPolicy = StandardFXOutputPolicy

  lazy val pane = new FXSimulationPane(drawer) with FXSelectionArea with KeyboardManager

  lazy val cont = new FXSimulationWindow(new HBox,pane,true)
  override def keyboard: AbstractKeyboardManager = pane

  override def selection: Option[AbstractSelectionArea] = Some(pane)

  override def container: Container[SimulationView] = cont

  override def init(): Unit = {
    initializeScalaFXPlatform
    Platform.runLater {
      cont
    }
  }
}
