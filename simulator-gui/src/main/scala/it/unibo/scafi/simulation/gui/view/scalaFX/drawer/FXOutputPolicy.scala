package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import it.unibo.scafi.simulation.gui.view.OutputPolicy

trait FXOutputPolicy extends OutputPolicy{
  type OUTPUT_NODE <: javafx.scene.Node
}
