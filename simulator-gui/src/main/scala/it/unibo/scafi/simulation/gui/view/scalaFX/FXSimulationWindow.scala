package it.unibo.scafi.simulation.gui.view.scalaFX


import it.unibo.scafi.simulation.gui.view.scalaFX.pane.PannablePane
import it.unibo.scafi.simulation.gui.view.{GraphicsOutput, Window}

import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.stage.Stage
//TODO
class SimulationWindow(infoPane : HBox,
                       simulationPane : FXSimulationPane) extends Stage with Window {
  private val PADDING = 20
  this.title = name
  val pane = new BorderPane {
    padding = Insets(PADDING)
    this.minWidth = 800
    this.minHeight = 600
  }
  infoPane.padding = Insets(PADDING)
  pane.setTop(infoPane)
  val pannablePane = new PannablePane(simulationPane,Some(pane))
  pane.setCenter(pannablePane)
  scene  = new Scene {
    content = pane
  }
  pane.prefWidthProperty().bind(this.getScene().widthProperty())
  pane.prefHeightProperty().bind(this.getScene().heightProperty())
  override type OUTPUT = GraphicsOutput

  override def name: String = "Simulation pane"

  override def close: Unit = this.close()

  override def output: Set[OUTPUT] = ???
}
