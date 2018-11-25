package it.unibo.scafi.simulation.frontend.view.scalaFX

import it.unibo.scafi.simulation.frontend.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.frontend.view._
import it.unibo.scafi.simulation.frontend.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutput}
import it.unibo.scafi.simulation.frontend.view.scalaFX.pane.FXSimulationPane

import scalafx.application.Platform

/**
  * standard fx view environment for simulation view
  */
object ScalaFXEnvironment extends ViewEnvironment[SimulationView] {
  lazy val standardConfiguration = WindowConfiguration.apply(800,600)
  var windowConfiguration : WindowConfiguration = standardConfiguration
  //standard value of fx application
  var drawer : FXOutputPolicy = StandardFXOutput
  //simulation pane
  private lazy val pane = new FXSimulationPane(drawer)
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
    windowConfiguration = ViewSetting.windowConfiguration
    initializeScalaFXPlatform()
    Platform.runLater {
      cont
    }
  }
}
