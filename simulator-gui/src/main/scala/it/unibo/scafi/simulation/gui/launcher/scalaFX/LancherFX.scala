package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.controller.SimpleInputController
import it.unibo.scafi.simulation.gui.incarnation.scafi.{ScafiLikeWorld, SimpleScafiWorld}
import it.unibo.scafi.simulation.gui.launcher.scalaFX.Launcher.world
import it.unibo.scafi.simulation.gui.model.core.World

object LancherFX {
  /**
    * used to initialize scalafx
    */
  new JFXPanel()
  /**
    * the world use to show thing
    */
  var world : ScafiLikeWorld = SimpleScafiWorld
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
  /**
    * the input controller used to controls input
    */
  var inputController = new SimpleInputController[ScafiLikeWorld](world)


}
