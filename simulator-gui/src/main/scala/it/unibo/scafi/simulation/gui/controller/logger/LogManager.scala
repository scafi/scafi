package it.unibo.scafi.simulation.gui.controller.logger

import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, Source}

/**
  * a manager of logger observer, each message is send to all
  * log observer, each one(looking the priority) decide to log or
  * not the message
  */
object LogManager extends Source {

  override type O = LogObserver

  trait LogObserver extends Observer {

    override final def !!(event: Event): Unit = {
      event match {
        case n:LogEvent => logging(n.message,n.priority)
      }
    }
    protected def logging(message : String, priority: Priority)
  }
  /**
    * define the priority of the message
    */
  sealed trait Priority
  object High extends Priority
  object Middle extends Priority
  object Low extends Priority

  private case class LogEvent(message:String, priority: Priority) extends Event

  def log(message : String, priority: Priority): Unit = {
    this.!!!(LogEvent(message,priority))
  }

}
