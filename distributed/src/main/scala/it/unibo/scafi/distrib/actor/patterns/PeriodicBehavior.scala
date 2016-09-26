package it.unibo.scafi.distrib.actor.patterns

/**
 * @author Roberto Casadei
 *
 */

import akka.actor.Actor
import it.unibo.scafi.distrib.actor.GoOn

import scala.concurrent.duration._

/**
 * Represents a behavior that marks some repetitive work via self messages
 * (by default [[GoOn]]). Defines a utility method for the purpose
 * ({{ScheduleNextWorkingCycle}}) and some template methods/hooks for lifecycle.
 */
trait LifecycleBehavior { thisVery: Actor =>
  def HandleLifecycle(): Unit

  import context.dispatcher

  def ScheduleNextWorkingCycle(delay: FiniteDuration, msg: Any = GoOn): Unit = {
    context.system.scheduler.scheduleOnce(delay){ this.self ! msg }
  }

  def LifecyclePreStart() { }
  def LifecyclePostStop() { }
}

/**
 * Represents a periodic or repeatedly-delayed behavior.
 *   - {{HandleLifecycle}} schedules the next job with a delay given by {{workInterval}
 *   - {{LifecyclePreStart}} handles the {{initialDelay}}
 */
trait PeriodicBehavior extends LifecycleBehavior { thisVery: Actor =>
  val initialDelay: Option[FiniteDuration] = None
  var workInterval: FiniteDuration

  def HandleLifecycle() = {
    ScheduleNextWorkingCycle(workInterval)
  }

  def ScheduleNextWorkingCycle(): Unit = {
    ScheduleNextWorkingCycle(workInterval, GoOn)
  }

  override def LifecyclePreStart(): Unit = {
    super.LifecyclePreStart()
    PeriodicBehaviorPreStart()
  }

  def PeriodicBehaviorPreStart() = initialDelay.foreach(ScheduleNextWorkingCycle(_))
}
