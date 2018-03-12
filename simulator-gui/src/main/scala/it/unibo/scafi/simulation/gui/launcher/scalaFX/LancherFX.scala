package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.incarnation.scafi.SimpleScafiWorld
import it.unibo.scafi.simulation.gui.model.core.World

object LancherFX {
  /**
    * used to initialize scalafx
    */
  new JFXPanel()
  /**
    * the world use to show thing
    */
  var world : World = SimpleScafiWorld
  /**
    * nodes show in the display
    */
  var nodes = 0
  /** radius of neighbours
    *
    */
  var radius = 0
  /**
    * the period of main scheduler
    */
  var tick = 100
  /**
    * render of not the neighbours
    */
  var neighboursRender = false
}
