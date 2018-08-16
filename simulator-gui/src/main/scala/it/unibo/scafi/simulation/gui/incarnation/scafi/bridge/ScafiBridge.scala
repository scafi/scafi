package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.configuration.command.Command
import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiLikeWorld, scafiWorld}
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

import scala.util.Random
/**
  * define a generic bridge with scafi
  */
abstract class ScafiBridge extends ExternalSimulation[ScafiLikeWorld]("scafi-bridge") {
  override type EXTERNAL_SIMULATION = SpaceAwareSimulator
  override type SIMULATION_PROTOTYPE = () => EXTERNAL_SIMULATION
  override type SIMULATION_CONTRACT = ExternalSimulationContract

  /**
    * the world
    */
  val world = scafiWorld
  /**
    * current simulation prototype, at begging no prototype defined
    */
  var simulationPrototype: Option[SIMULATION_PROTOTYPE] = None
  /**
    * action used to produced changes into internal world
    */
  var action : PartialFunction[EXPORT,(ScafiLikeWorld,ID)=>Unit] = Actions.generalAction
  //scafi execution context
  private var context : Option[CONTEXT=>EXPORT] = None

  /**
    * @return current running context (if it is defined)
    */
  protected def runningContext : CONTEXT=>EXPORT = {
    require(context.isDefined)
    context.get
  }

  private var _program : Option[Class[_]] = None

  /**
    * @return simulation program
    */
  def program : Option[Class[_]] = _program

  /**
    * setter of program, if program don't is class of CONTEXT => EXPORT the program don't set
    * @param programClass
    */
  def program_=(programClass : Class[_]): Unit = {
    if(programClass.newInstance().isInstanceOf[CONTEXT=>EXPORT]) {
      this._program = Some(programClass)
    }
  }

  //describe scafi contract like
  override val contract = new ExternalSimulationContract {
    private var currentSimulation : Option[EXTERNAL_SIMULATION] = None
    override def simulation: Option[SpaceAwareSimulator] = this.currentSimulation

    override def initialize(prototype: SIMULATION_PROTOTYPE): Unit = {
      //to initialize the simulation, current simulation must be empty and program must be definied
      require(currentSimulation.isEmpty && program.isDefined)
      //create context by program passed
      context = Some(program.get.newInstance().asInstanceOf[CONTEXT=>EXPORT])
      //create new simulation
      this.currentSimulation = Some(createSimulation(world,prototype))
    }

    override def restart(prototype: SIMULATION_PROTOTYPE): Unit = {
      //to restart simulation current simulation must be defined
      require(currentSimulation.isDefined)
      //create the instance of program
      context = Some(program.get.newInstance().asInstanceOf[CONTEXT=>EXPORT])
      //set current simulation to another
      this.currentSimulation = Some(createSimulation(world,prototype))
    }
    private def createSimulation(w : ScafiLikeWorld, p : SIMULATION_PROTOTYPE) : SpaceAwareSimulator = {
      p()
    }
  }
}