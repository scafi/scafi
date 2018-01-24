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
  protected val minDelta : Int
  protected val maxDelta : Int
  private var stopped = true
  protected var currentExecutor : ActorExecutor
  //TEMPLATE METHOD
  protected def AsyncLogicExecution() : Unit

  protected class ActorExecutor extends Thread {
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
  def increaseDelta(delta : Int) = require(delta > 0 && this.delta + delta < maxDelta); this.delta += delta

  /**
    * decrease the velocity of simulation
    * @param delta remove to current delta
    */
  def decreaseDelta(delta : Int) = require(delta > 0 && this.delta - delta > minDelta);  this.delta -= delta

  def isStopped : Boolean = stopped
}
