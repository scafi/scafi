package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.SchedulerObserver
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld.WorldObserver
import it.unibo.scafi.simulation.gui.model.core.Node

trait Controller extends SchedulerObserver
trait WorldController[N <: Node] extends Controller with WorldObserver[N]
