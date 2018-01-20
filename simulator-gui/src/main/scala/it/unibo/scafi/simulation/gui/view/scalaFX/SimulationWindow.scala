package it.unibo.scafi.simulation.gui.view.scalaFX


import it.unibo.scafi.simulation.gui.view.{GraphicsOutput, SimulationOutput, Window}

import scalafx.geometry.Insets
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.{Group, Scene}
import scalafx.stage.Stage
//TODO
class SimulationWindow(infoPane : VBox,
                       simulationPane : SimulationOutput with GraphicsOutput with Group) extends Stage with Window {
  private val PADDING = 20
  this.title = name
  val pane = new BorderPane {
    padding = Insets(PADDING)
  }
  infoPane.padding = Insets(PADDING)

  simulationPane.setStyle("-fx-border-color: black");
  infoPane.setFillWidth(true)

  pane.setTop(infoPane)

  pane.setCenter(simulationPane)
  scene = new Scene {
    content = pane
  }
  this.setFullScreen(true)
  this.setMaximized(true)

  override type OUTPUT = GraphicsOutput

  override def name: String = "Simulation pane"

  override def close: Unit = this.close()

  override def output: Set[OUTPUT] = ???
}
