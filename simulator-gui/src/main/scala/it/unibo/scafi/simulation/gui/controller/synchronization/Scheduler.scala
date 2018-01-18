package it.unibo.scafi.simulation.gui.controller.synchronization

import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, PrioritySource, Source}

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
trait Scheduler extends Source {
  override type O = Scheduler.SchedulerObserver

  def start()

  def stop()

  /**
    * time in mills of each tick
    * @return the time
    */
  def delta() : Int
}

object Scheduler {
  object scheduler extends Scheduler with PrioritySource {
    private[this] var d : Int = _
    private[this] var on = false
    private[this] var end = true
    private[this] val mainThread = new Runnable {
      private[this] var elapsed = delta
      override def run(): Unit = {
        while(on) {
          val t0 = System.currentTimeMillis()
          scheduler.!!!(Tick(delta))
          val t1 = System.currentTimeMillis()
          val wait = if (t1 - t0 > delta) {
            0
          } else {
            delta - (t1 - t0)
          }
          Thread.sleep(wait)
        }
        end = true
      }
    }
    override def start(): Unit = {
      require(delta > 0 && end)
      on = true
      new Thread(mainThread).start()
    }

    override def stop(): Unit = {
      on = false
    }

    def delta_=(v : Int) = {
      require(v > 0)
      d = v
    }
    def delta = d

  }
  //factory (singleton like)
  def apply: Scheduler = scheduler
  /**
    * the scheduler event
    * @param timeElapsed the time elapsed since the last call
    */
  case class Tick(timeElapsed : Float) extends Event

  trait SchedulerObserver extends Observer{
    override def !!(event: Event): Unit = {
      event match {
        case Tick(t) => this.onTick(t)
        case _ => super.!!(event)
      }
    }
    //TEMPLATE METHOD
    def onTick(float: Float)
  }
}
