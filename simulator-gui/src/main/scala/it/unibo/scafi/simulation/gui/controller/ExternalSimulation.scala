package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.core.World

/**
  * define an external simulation that controls the current world
  * @tparam W
  */
trait ExternalSimulation[W <: AggregateWorld] extends AsyncLogicController[W]{
  //BRIDGE
  type SIMULATION[S,P] <: SimulationContract[S,W,P]
  protected val world : W
  //factory method

  /**
    * start the external simulation with the default seed
    */
  def getContract[S,P]  : SIMULATION[S,P]

  def simulationPrototype[P] : P
  override def start(): Unit = {
    require(isStopped)
    getContract.initialize(world,simulationPrototype)
    super.start()
  }
  /**
    * restart the simulation with another seed
    */
  def restart() = {
    require(getContract.getSimulation.isDefined)
    super.stop()
    getContract.restart(world,simulationPrototype)
    super.start()
  }

  /**
    * restart a simulation stopped
    * @throws IllegalArgumentException if there isn't simulation stopped
    */
  def continue() = {
    require(isStopped && getContract.getSimulation.isDefined)
    super.start()
  }
}

/**
  * a contract to an external simulation
  * @tparam S the type of external
  * @tparam W the type of internal world
  */
trait SimulationContract[S,W<: World,P] {
  /**
    * get the current external simulation
    * @return None if the simulation isn't initialize, Some otherwhise
    */
  def getSimulation : Option[S]

  /**
    * initialize the external simulation
    * @throws IllegalStateException if the simulation is already initialized
    * @param world the internal representation of the world
    */
  def initialize(world : W,prototype : P)

  /**
    * restart the external simulation
    * @throws IllegalStateException if the simulation is never initialized
    * @param world the internal representation of the world
    */
  def restart(world : W,prototype : P)
}