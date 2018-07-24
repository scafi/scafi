package it.unibo.scafi.simulation.gui.controller.presenter

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.view.View

/**
  * controls the world and update the output
  */
trait Presenter[W <: AggregateWorld, OUTPUT <: View] extends Controller[W] {
  /**
    * add output to current presenter
    * @param view the output where presenter put changes
    */
  def output(view : OUTPUT)
}

