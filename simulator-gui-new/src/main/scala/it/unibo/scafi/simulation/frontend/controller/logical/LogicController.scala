package it.unibo.scafi.simulation.frontend.controller.logical

import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.model.aggregate.AggregateWorld

/**
  * a controller that has a defined logic to change
  * the world
 *
  * @tparam W the world observed
  */
trait LogicController[W <: AggregateWorld] extends Controller[W] {
  /**
    * start the internal logic
    * @throws IllegalStateException if the simulation is started
    */
  def start() : Unit

  /**
    * stop the internal logic
    * @throws IllegalStateException if the simulation is stopped
    */
  def stop() : Unit
}

