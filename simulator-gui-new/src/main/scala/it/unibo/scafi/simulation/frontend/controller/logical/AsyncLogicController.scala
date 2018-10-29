package it.unibo.scafi.simulation.frontend.controller.logical

import java.util.Date
import java.util.concurrent.TimeUnit

import it.unibo.scafi.simulation.frontend.controller.logger.LogManager.{Channel, IntLog}
import it.unibo.scafi.simulation.frontend.model.aggregate.AggregateWorld

/**
  * a controller with internal async logic
  * @tparam W the world observed
  */
trait AsyncLogicController[W <: AggregateWorld] extends LogicController[W] {
  /**
    * @return the name of current async logic
    */
  def asyncLogicName : String
  /**
    * @return min delta value admissible
    */
  protected def minDelta : Int = 0
  /**
    * @return max delta value admissible
    */
  protected def maxDelta : Option[Int]
  /**
    * internal delta, start with min delta value
    */
  protected var delta : Int = minDelta

  private var stopped = true
  /**
    * current executor of async logic
    */
  protected var currentExecutor : ActorExecutor = new ActorExecutor()
  //TEMPLATE METHOD
  /**
    * describe internal async logic
    */
  protected def asyncLogicExecution() : Unit

  /**
    * internal class used to describe a text that run async logic execution
    */
  protected class ActorExecutor extends Thread {
    private var time = 0L
    private var tick = 0
    //set the name of thread with the logic name
    this.setName(asyncLogicName)
    override def run(): Unit = {
      time = System.currentTimeMillis()
      while(!stopped) {
        //do logic execution
        asyncLogicExecution()
        if(delta != 0) {
          TimeUnit.MILLISECONDS.sleep(delta)
        }
        tick += 1
        if(System.currentTimeMillis() - time > 1000) {
          import it.unibo.scafi.simulation.frontend.controller.logger.{LogManager => log}
          log.notify(IntLog(Channel.SimulationRound, "instant", tick))
          tick = 0
          time = System.currentTimeMillis()
        }
      }
    }
  }

  /**
    * start async logic it must be stopped
    */
  def start() : Unit = {
    require(stopped)
    stopped = false
    currentExecutor.start()
  }

  /**
    * stop the current async logic, it must be don't stopped
    */
  def stop() : Unit = {
    require(!stopped)
    stopped = true
    currentExecutor = new ActorExecutor
  }

  /**
    * increase the velocity of async logic
    * @param delta add to the current delta
    */
  def increaseDelta(delta : Int) : Unit = {
    require(delta > 0)
    this.delta = if(maxDelta.isDefined && maxDelta.get > this.delta + delta) maxDelta.get else this.delta + delta
  }

  /**
    * decrease the velocity of simulation
    * @param delta remove to current delta
    */
  def decreaseDelta(delta : Int) : Unit = {
    require(delta > 0)
    this.delta = if(this.delta - delta < minDelta) minDelta else this.delta - delta
  }

  /**
    * tell if the async logic is stopped or not
    * @return true if the logic is stopped false otherwise
    */
  def isStopped : Boolean = stopped
}
