package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.SchedulerObserver
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld.WorldObserver
import it.unibo.scafi.simulation.gui.model.core.World

trait Controller extends SchedulerObserver

trait WorldController[W <: World] extends Controller {
  self : WorldObserver[W#NODE] =>
}


