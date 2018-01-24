package it.unibo.scafi.simulation.gui.view.scalaFX


import it.unibo.scafi.simulation.gui.view.{GraphicsOutput, Window}

import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.stage.Stage
//TODO
class SimulationWindow(infoPane : HBox,
                       simulationPane : FXSimulationPane) extends Stage with Window {
  private val PADDING = 20
  this.title = name
  val pane = new BorderPane {
    padding = Insets(PADDING)
  }
  infoPane.padding = Insets(PADDING)
  pane.setTop(infoPane)
  val scrollPane = new ScrollPane{
    content = simulationPane
    this.prefHeight = 800
    this.prefWidth = 1500
  }
  pane.setCenter(scrollPane)
  scene = new Scene {
    content = pane
  }
  override type OUTPUT = GraphicsOutput

  override def name: String = "Simulation pane"

  override def close: Unit = this.close()

  override def output: Set[OUTPUT] = ???
}
