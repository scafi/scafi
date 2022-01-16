package it.unibo.scafi.simulation.s2.frontend.controller

import it.unibo.scafi.simulation.s2.frontend.controller.synchronization.Scheduler.SchedulerObserver
import it.unibo.scafi.simulation.s2.frontend.model.common.world.ObservableWorld

/**
 * the root trait of all controller a controller controls a world
 * @tparam W
 *   the world observed
 */
trait Controller[W <: ObservableWorld] extends SchedulerObserver
