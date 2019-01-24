package it.unibo.scafi.simulation.frontend.model

/**
  * Created by chiara on 14/11/16.
  */
trait Action {
  def name: String

  def action: Any
}