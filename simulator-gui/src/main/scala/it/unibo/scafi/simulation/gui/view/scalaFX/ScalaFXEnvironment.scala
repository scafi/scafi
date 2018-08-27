package it.unibo.scafi.simulation.gui.view.scalaFX

import java.awt.Toolkit
import javafx.application.Application
import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.gui.view.scalaFX.common.{FXSelectionArea, KeyboardManager}
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutputPolicy}
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.FXSimulationPane
import it.unibo.scafi.simulation.gui.view._

import scalafx.application.Platform
import scalafx.scene.layout.HBox

/**
  * standard fx view eniromento
  */
object ScalaFXEnvironment extends ViewEnvironment[SimulationView] {
  private lazy val standardConfiguration = WindowConfiguration.apply(800,600)
  var windowConfiguration : WindowConfiguration = standardConfiguration
  //standard value of fx application
  var drawer : FXOutputPolicy = StandardFXOutputPolicy
  //simulation pane
  private lazy val pane = new FXSimulationPane(drawer) with FXSelectionArea with KeyboardManager
  //main container
  private lazy val cont = new FXSimulationWindow(pane,true,windowConfiguration)
  //keyboard manager
  override def keyboard: AbstractKeyboardManager = pane
  //selection area manager
  override def selection: Option[AbstractSelectionArea] = Some(pane)
  //view container
  override def container: Container[SimulationView] = cont
  //initialize fx environment
  override def init(): Unit = {
    initializeScalaFXPlatform
    Platform.runLater {
      cont
    }
  }
}
