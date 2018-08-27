package it.unibo.scafi.simulation.gui.view.scalaFX

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.{Log, LogObserver}
import it.unibo.scafi.simulation.gui.pattern.observer.Event

import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Tab, TabPane}
import scalafx.scene.layout.VBox
import scalafx.stage.Stage
import scalafx.Includes._
object FXLogger extends Stage with LogObserver {
  private val pane = new TabPane()
  private var channel : Set[String] = Set.empty
  this.scene = new Scene {
    content = pane
  }
  this.width = 800
  this.height = 600
  pane.minWidth = 800

  /**
    * store the event received
    *
    * @param event the event produced
    */
  override def update(event: Event): Unit = {
    event match {
      case log : Log[_] => if(!channel.contains(log.channel)) {
        Platform.runLater{
          val tab = new Tab {
            text = log.channel
          }
          this.pane.tabs += tab
          this.channel += log.channel
        }
      }
    }
  }
  LogManager.attach(this)
}
