package it.unibo.scafi.simulation.gui.model

/**
  * Created by chiara on 14/11/16.
  */
trait Action {
  def name: String

  def action: Any
}