package it.unibo.scafi.simulation.frontend.controller.logical

import it.unibo.scafi.simulation.frontend.model.aggregate.AggregateWorld

/**
  * define an external simulation that controls the current world
 *
  * @tparam W the internal world representation
  */
abstract class ExternalSimulation[W <: AggregateWorld](override val asyncLogicName : String) extends AsyncLogicController[W]{
  //BRIDGE, external simulation type
  type EXTERNAL_SIMULATION
  //BRIDGE, a prototype to create simulation
  type SIMULATION_PROTOTYPE
  //BRIDGE, a contract used to create simulation
  type SIMULATION_CONTRACT <: ExternalSimulationContract

  /**
    * @return the simulation world
    */
  protected def world : W
  //factory method
  /**
    * the contract used to create external simulation
    * @return the contract used to create external simulation
    */
  def contract  : SIMULATION_CONTRACT
  /**
    * @return the current simulation prototype
    */
  def simulationPrototype : Option[SIMULATION_PROTOTYPE]

  /**
    * initialize the external simulation
    */
  def init() : Unit = {
    //to initialize the simulation the prototype must be defined
    require(simulationPrototype.isDefined)
    //use contract to initialize the simulation
    contract.initialize(simulationPrototype.get)
  }

  override def start(): Unit = {
    //verify if the contract is defined, otherwise the simulation can't start
    require(contract.simulation.isDefined)
    super.start()
  }
  /**
    * restart the simulation with another seed
    */
  def restart() : Unit = {
    //verify if the contract and the prototype is defined, other the simulation can't restart
    require(contract.simulation.isDefined && simulationPrototype.isDefined)
    //stop the current simulation (if isn't already stopped
    if(!this.isStopped) {
      super.stop()
    }
    //use contact to restart simulation
    contract.restart(simulationPrototype.get)
    //start the simulation
    super.start()
  }

  /**
    * restart a simulation stopped
    * @throws IllegalArgumentException if there isn't simulation stopped
    */
  def continue() : Unit = {
    require(isStopped && contract.simulation.isDefined)
    super.start()
  }

  /**
    * describe a contract used to create external simulation
    */
  trait ExternalSimulationContract {
    /**
    * get the current external simulation
    *
    * @return None if the simulation isn't initialize, Some otherwise
    */
    def simulation: Option[EXTERNAL_SIMULATION]

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