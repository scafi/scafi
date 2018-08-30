package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.scene.input.KeyCombination

import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.WindowConfiguration.{FullScreen, Window}
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.Logo

import scalafx.scene.image.ImageView
import scalafx.scene.layout.Pane
import scalafx.stage.Stage
import scalafx.Includes._

/**
  * a stage with an icon ad logo
  * @param window the window configuration used to set up stage
  */
private [scalaFX] class LogoStage(window: WindowConfiguration) extends Stage {
  window.size match {
    case FullScreen => {
      this.fullScreen = true
      this.fullScreenExitKey = KeyCombination.NO_MATCH
    }
    case Window(w,h) => {
      this.width = w
      this.minWidth = w
      this.height = h
      this.minHeight = h
    }
  }
  protected val logo = window.logoPath match{
    case Some(name : String) => new ImageView(Logo.big(name))
    case _ => new Pane()
  }

  window.iconPath match {
    case Some(name : String) => this.getIcons.add(Logo.small(name))
    case _ =>
  }
}
