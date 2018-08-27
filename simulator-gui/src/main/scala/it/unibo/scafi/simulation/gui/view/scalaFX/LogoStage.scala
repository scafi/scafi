package it.unibo.scafi.simulation.gui.view.scalaFX

import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.Logo

import scalafx.scene.image.ImageView
import scalafx.scene.layout.Pane
import scalafx.stage.Stage

class LogoStage(window: WindowConfiguration) extends Stage {

  protected val logo = window.logoPath match{
    case Some(name : String) => new ImageView(Logo.big(name))
    case _ => new Pane()
  }

  window.iconPath match {
    case Some(name : String) => this.getIcons.add(Logo.small(name))
    case _ =>
  }
}
