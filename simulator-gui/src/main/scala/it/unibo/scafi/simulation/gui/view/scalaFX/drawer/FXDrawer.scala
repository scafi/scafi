package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import it.unibo.scafi.simulation.gui.view.Drawer

trait FXDrawer extends Drawer{
  type OUTPUTNODE <: javafx.scene.Node
}
