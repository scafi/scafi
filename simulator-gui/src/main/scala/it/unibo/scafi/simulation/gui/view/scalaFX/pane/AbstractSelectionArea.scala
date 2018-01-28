package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.model.core.World
/**
  * define a trait used to select and clear item
  */
trait AbstractSelectionArea {
  /**
    * @return get the ids of nodes selected
    */
  def selected : Set[World#ID]
}
