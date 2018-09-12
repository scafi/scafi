package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import it.unibo.scafi.simulation.gui.view.OutputPolicy

/**
  * standard fx output policy
  */
trait FXOutputPolicy extends OutputPolicy{
  type OUTPUT_NODE <: javafx.scene.Node
}
