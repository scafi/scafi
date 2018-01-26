package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.model.core.World

import scalafx.scene.Node

/**
  * define a trait used to select and clear item
  */
trait AbstractSelectionArea {
  def moved : Set[World#ID]
}
