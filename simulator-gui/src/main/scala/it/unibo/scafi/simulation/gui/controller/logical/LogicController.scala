package it.unibo.scafi.simulation.gui.controller.logical

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * a controller that has a defined logic to change
  * the world
 *
  * @tparam W the world observed
  */
trait LogicController[W <: AggregateWorld] extends Controller[W] {
  /**
    * start the internal logic
    * @throws IllegalStateException if the simulation is started
    */
  def start

  /**
    * stop the internal logic
    * @throws IllegalStateException if the simulation is stopped
    */
  def stop
}

trait AsyncLogicController[W <: AggregateWorld] extends LogicController[W]{
  protected var delta : Int
  protected val threadName : String
  protected val minDelta : Int = 0
  protected val maxDelta : Option[Int]
  private var stopped = true
  protected var currentExecutor : ActorExecutor
  //TEMPLATE METHOD
  protected def AsyncLogicExecution() : Unit

  protected class ActorExecutor extends Thread {
    this.setName(threadName)
    override def run(): Unit = {
      while(!stopped) {
        AsyncLogicExecution()
        Thread.sleep(delta)
      }
    }
  }
  def start() = {
    require(stopped)
    currentExecutor = new ActorExecutor
    stopped = false
    currentExecutor.start()

  }
  def stop() = {
    require(!stopped)
    stopped = true
  }

  /**
    * increase the velocity of async logic
    * @param delta add to the current delta
    */
  def increaseDelta(delta : Int) = {
    if(maxDelta.isDefined) {
      require(delta > 0 && this.delta + delta < maxDelta.get); this.delta += delta
    }
  }

  /**
    * decrease the velocity of simulation
    * @param delta remove to current delta
    */
  def decreaseDelta(delta : Int) = require(delta > 0 && this.delta - delta > minDelta);  this.delta -= delta

  /**
    * tell if the async logic is stopped or not
    * @return true if the logic is stopped false otherwise
    */
  def isStopped : Boolean = stopped
}
