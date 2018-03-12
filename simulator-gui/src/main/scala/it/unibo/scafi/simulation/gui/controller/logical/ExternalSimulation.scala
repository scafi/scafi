package it.unibo.scafi.simulation.gui.controller.logical

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.core.World

/**
  * define an external simulation that controls the current world
  * @tparam W
  */
trait ExternalSimulation[W <: AggregateWorld] extends AsyncLogicController[W]{
  //BRIDGE
  type EXTERNAL_SIMULATION
  type SIMULATION_PROTOTYPE
  type SIMULATION_CONTRACT <: SimulationContract[EXTERNAL_SIMULATION,W,SIMULATION_PROTOTYPE]
  protected val world : W
  //factory method

  /**
    * start the external simulation with the default seed
    */
  def contract  : SIMULATION_CONTRACT

  def simulationPrototype : SIMULATION_PROTOTYPE

  /**
    * initialize the external simulation, this operation must be before all thing
    */
  def init() = contract.initialize(world,simulationPrototype)

  override def start(): Unit = {
    require(contract.getSimulation.isDefined)
    super.start()
  }
  /**
    * restart the simulation with another seed
    */
  def restart() = {
    require(contract.getSimulation.isDefined)
    super.stop()
    contract.restart(world,simulationPrototype)
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

  /** TODO REPLACE WITH THIS
  trait ExternalSimulationContract[EXTERNAL] {
    /**
    * get the current external simulation
    *
    * @return None if the simulation isn't initialize, Some otherwhise
    */
    def getSimulation: Option[EXTERNAL]

    /**
    * initialize the external simulation
    *
    * @throws IllegalStateException if the simulation is already initialized
    * @param world the internal representation of the world
    */
    def initialize(world: W, prototype: SIMULATION_PROTOTYPE)

    /**
    * restart the external simulation
    *
    * @throws IllegalStateException if the simulation is never initialized
    * @param world the internal representation of the world
    */
    def restart(world: W, prototype: SIMULATION_PROTOTYPE)
  }
    */
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