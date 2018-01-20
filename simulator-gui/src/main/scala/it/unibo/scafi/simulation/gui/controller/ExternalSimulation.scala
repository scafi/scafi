package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

trait ExternalSimulation[W <: AggregateWorld] extends LogicController[W]{

  type SEED
  type ACTION = (W#ID) => Unit
  protected var actionToDo : List[ACTION] = List[ACTION]()

  /**
    * start the simulation with a seed
    */
  def start(s : SEED)

  /**
    * stop current simulation
    * @throws IllegalStateException if the simulation isn't start
    */
  def stop

  /**
    * restart the simulation with another seed
    */
  def restart(s : SEED)

  /**
    * restart a simulation stopped
    * @throws IllegalArgumentException if there isn't simulation stopped
    */
  def continue()

  /**
    * get the current timing of simulation
    */
  def delta()

  /**
    * increase the velocity of simulation
    */
  def increaseDelta()

  /**
    * decrease the velocity of simulation
    */
  def decreaseDelta()

  /**
    * define what to do each internal tick
    */
  protected def round()
}
