package it.unibo.scafi.simulation.gui.controller.logical

import it.unibo.scafi.simulation.gui.model.aggregate.implementation.immutable.AggregateWorld
import it.unibo.scafi.simulation.gui.model.core.World

/**
  * define an external simulation that controls the current world
  * @tparam W
  */
trait ExternalSimulation[W <: AggregateWorld] extends AsyncLogicController[W]{
  //BRIDGE
  type EXTERNAL_SIMULATION
  type SIMULATION_PROTOTYPE
  type SIMULATION_CONTRACT <: ExternalSimulationContract
  protected val world : W
  //factory method

  /**
    * start the external simulation with the default seed
    */
  def contract  : SIMULATION_CONTRACT

  def simulationPrototype : Option[SIMULATION_PROTOTYPE]

  /**
    * initialize the external simulation, this operation must be before all thing
    */
  def init() = {
    require(simulationPrototype.isDefined)
    contract.initialize(simulationPrototype.get)
  }

  override def start(): Unit = {
    require(contract.getSimulation.isDefined)
    super.start()
  }
  /**
    * restart the simulation with another seed
    */
  def restart() = {
    require(contract.getSimulation.isDefined && simulationPrototype.isDefined)
    super.stop()
    contract.restart(simulationPrototype.get)
    super.start()
  }

  /**
    * restart a simulation stopped
    * @throws IllegalArgumentException if there isn't simulation stopped
    */
  def continue() = {
    require(isStopped && contract.getSimulation.isDefined)
    super.start()
  }

  trait ExternalSimulationContract {
    /**
    * get the current external simulation
    *
    * @return None if the simulation isn't initialize, Some otherwhise
    */
    def getSimulation: Option[EXTERNAL_SIMULATION]

    /**
      * initialize the external simulation
      * @throws IllegalStateException if the simulation is already initialized
      * @param prototype the prototype used to create simulation
      */
    def initialize(prototype: SIMULATION_PROTOTYPE)

    /**
    * restart the external simulation
    *
    * @throws IllegalStateException if the simulation is never initialize
      * @param prototype the prototype used to restart simulation
    */
    def restart(prototype: SIMULATION_PROTOTYPE)
  }
}