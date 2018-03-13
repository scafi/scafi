package it.unibo.scafi.simulation.gui.view.scalaFX

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.LogObserver

import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.stage.Stage

class FXLogger extends Stage with LogObserver {
  val view = new VBox()
  this.title = "Log"
  this.scene = new Scene {
    content = view
  }
  //TODO MAGIC NUMBER TO REMOVE
  this.width = 800
  this.height = 600
  override protected def logging(message: String, priority: LogManager.Priority): Unit = Platform.runLater {
    view.children.add(new Label(message))
  }
}
