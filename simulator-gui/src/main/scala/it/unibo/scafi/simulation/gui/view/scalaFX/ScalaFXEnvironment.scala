package it.unibo.scafi.simulation.gui.view.scalaFX

import com.sun.javafx.css.StyleManager
import it.unibo.scafi.simulation.gui.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.gui.view._
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutputPolicy}
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.FXSimulationPane

import scalafx.application.Platform

/**
  * standard fx view eniromento
  */
object ScalaFXEnvironment extends ViewEnvironment[SimulationView] {
  lazy val standardConfiguration = WindowConfiguration.apply(800,600)
  var windowConfiguration : WindowConfiguration = standardConfiguration
  //standard value of fx application
  var drawer : FXOutputPolicy = StandardFXOutputPolicy
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
    initializeScalaFXPlatform
    Platform.runLater {
      val container = cont
    }
  }
}
