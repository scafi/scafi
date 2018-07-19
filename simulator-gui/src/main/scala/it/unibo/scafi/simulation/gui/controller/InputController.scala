package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.implementation.immutable.AggregateWorld

/**
  * an input controller for aggregate world
 *
  * @tparam W the type of the world
  */
trait InputController[W <: AggregateWorld] extends Controller[W]

