package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import it.unibo.scafi.simulation.gui.view.OutputPolicy

trait FXOutputPolicy extends OutputPolicy{
  type OUTPUTNODE <: javafx.scene.Node
}
