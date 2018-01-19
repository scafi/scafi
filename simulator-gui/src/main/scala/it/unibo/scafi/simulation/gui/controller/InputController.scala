package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * an input controller for aggregate world
  *
  * @param world to controls
  * @tparam W the type of the world
  */
abstract class InputController[W <: AggregateWorld](world : W)

