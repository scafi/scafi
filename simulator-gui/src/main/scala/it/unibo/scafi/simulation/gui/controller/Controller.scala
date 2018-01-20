package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.SchedulerObserver
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld

/**
  * the root trait of all controller
  */
trait Controller extends SchedulerObserver

/**
  * the root type of all controller that observer a world
  * @tparam W the world observed
  */
trait WorldController[W <: ObservableWorld] extends Controller

