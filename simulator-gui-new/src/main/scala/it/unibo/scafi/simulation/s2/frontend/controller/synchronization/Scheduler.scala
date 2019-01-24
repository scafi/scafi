package it.unibo.scafi.simulation.s2.frontend.controller.synchronization

import it.unibo.utils.observer.{Event, Observer, PrioritySource, Source}

/**
  * Scheduler has internal frequency used to
  * notify his observer.
  * the observers are Controller,
  * each controller must be change the world
  * only when scheduler notify it.
  * the main reason of this structure is to separate
  * the model update with external update.
  * i want to create a structure game loop like.
  */
trait Scheduler extends Source {
  override type O = Scheduler.SchedulerObserver

  /**
    * start to schedule controller
    */
  def start()

  /**
    * stop to schedule controller
    */
  def stop()

  /**
    * time in mills of each tick
    * @return the time
    */
  def delta : Int

  /**
    * setter of delta value
    * @param d new delta value
    */
  def delta_=(d : Int)
}

object Scheduler {
  object scheduler extends Scheduler with PrioritySource {
    private[this] var d : Int = _
    private[this] var on = false
    private[this] var end = false
    private[this] val mainThread = new Runnable {
      override def run(): Unit = {
        while(on) {
          val t0 = System.currentTimeMillis()
          scheduler notify Tick(delta)
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
      require(delta > 0 && !end)
      on = true
      val thread : Thread = new Thread(mainThread)
      thread.setName("scafi-gui-scheduler")
      thread.start()
    }

    override def stop(): Unit = {
      on = false
    }

    def delta_=(v : Int) : Unit = {
      require(v > 0)
      d = v
    }
    def delta : Int = d

  }
  //factory (singleton like)
  def apply: Scheduler = scheduler
  /**
    * the scheduler event
    * @param timeElapsed the time elapsed since the last call
    */
  case class Tick(timeElapsed : Float) extends Event

  /**
    * root class of all scheduler observer
    */
  trait SchedulerObserver extends Observer {
    override def update(event: Event): Unit = {
      event match {
        case Tick(t) => this.onTick(t)
        case _ =>
      }
    }
    //TEMPLATE METHOD
    def onTick(float: Float)
  }
}
