package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.incarnation.console.ConsoleWorld
import it.unibo.scafi.simulation.gui.launcher.console.ConsoleLogger
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.view.scalaFX.{FXSimulationPane, SimulationWindow}

import scala.util.Random
import scalafx.application.Platform
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox

object Launch extends App {
  val r = new Random()

  new JFXPanel()
  val world = new ConsoleWorld
  val node : Set[world.RootNode] = ((0 to 1000) map {
    new world.RootNode(_,Point2D(r.nextInt(1000),r.nextInt(500)))
  } toSet)
  world ++ node
  val pane = new FXSimulationPane()
  Platform.runLater {
    pane.out(node)
    val dialogStage = new SimulationWindow(new VBox(){
      children = Seq(new Button("PROVA"), new Button("ANCORA"))
    }, pane)
    // Show dialog and wait till it is closed
    dialogStage.show

    LogManager <-- (new ConsoleLogger)
  }
}
