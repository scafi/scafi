package it.unibo.scafi.simulation.gui.controller.synchronization

import it.unibo.scafi.simulation.gui.pattern.observer.{Event, PrioritySource}

/**
  * very important concept in this contest:
  * Scheduler has internal frequency used to
  * notify his observer.
  * the observer in this contest are Controller
  * each controller must be change the world
  * only when scheduler notify it.
  * the main reason of this structure is to separate
  * the model update with external update.
  * i want to create a structure game loop like.
  */
trait Scheduler extends PrioritySource {

}

object Scheduler {

  /**
    * the scheduler event
    * @param timeElapsed the time elapsed since the last call
    */
  case class Tick(timeElapsed : Float) extends Event
}
