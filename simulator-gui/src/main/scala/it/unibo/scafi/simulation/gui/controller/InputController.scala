package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * an input controller for aggregate world
 *
  * @tparam W the type of the world
  */
trait InputController[W <: AggregateWorld] extends Controller[W]

