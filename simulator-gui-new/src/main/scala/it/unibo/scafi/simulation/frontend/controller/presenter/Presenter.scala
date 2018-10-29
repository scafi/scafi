package it.unibo.scafi.simulation.frontend.controller.presenter

import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.frontend.view.View

/**
  * controls the world changes and update the output
  * based on changes
  */
trait Presenter[W <: AggregateWorld, OUTPUT <: View] extends Controller[W] {
  /**
    * add output to current presenter
    * @param view the output where presenter put changes
    */
  def output(view : OUTPUT)
}

