package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.CommandBinding
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.{PerformancePolicy, StandardPolicy}
import it.unibo.scafi.simulation.gui.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.gui.configuration.{Program, ProgramBuilder}
import it.unibo.scafi.simulation.gui.controller.presenter.SimulationPresenter
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiCommandBinding.StandardBinding
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiProgramEnvironment
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{ScafiSimulationInitializer, SimulationInfo}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiLikeWorld, ScafiWorldInitializer, scafiWorld}
import it.unibo.scafi.simulation.gui.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutput}
import it.unibo.scafi.simulation.gui.view.{OutputPolicy, SimulationView, WindowConfiguration}

/**
  * scafi program builder used to create scafi program
  * @param configuration the scafi configuration used to initialize program
  */
private class ScafiProgramBuilder(override val configuration: ScafiConfiguration) extends ProgramBuilder[ScafiConfiguration] {
  override def create: Program[_,_] = {
    val presenter = new SimulationPresenter[ScafiLikeWorld](scafiWorld,configuration.neighbourRender)
    //check if outputpolicy is supported
    val viewEnv : Option[ViewEnvironment[SimulationView]] = configuration.outputPolicy match {
      case policy : FXOutputPolicy => {
        ScalaFXEnvironment.drawer = policy
        Some(ScalaFXEnvironment)
      }
      case OutputPolicy.`noOutput` => None
      case _ => throw new IllegalArgumentException("output policy don't supported")
    }
    //set name, logo and icon to view environment
    if(viewEnv.isDefined) {
      viewEnv.get.windowConfiguration = ScafiWindowInfo(viewEnv.get.windowConfiguration)
    }
    //init the world
    configuration.worldInitializer.init(configuration.scafiWorldInfo)
    val bridged = configuration.simulationInitializer.create(configuration.scafiSimulationInformation)
    val programEnv = new ScafiProgramEnvironment(presenter,bridged,configuration.perfomance,configuration.logConfiguration)
    new Program[ScafiLikeWorld,SimulationView](programEnv, viewEnv,configuration.commandMapping)
  }
}
object ScafiProgramBuilder {
  /**
    * allow to create a program passing some parameter
    * @param scafiWorldInfo the world seed in scafi context
    * @param worldInitializer a world initializer to initialize scafi world
    * @param commandMapping a command mapping used to map some keyboard (and selection) input to an action
    * @param scafiSimulationInfo a seed used to set some parameter in scafi bridge simulation
    * @param simulationInitializer a simulation initializer used to initialize scafi bridge simulation
    * @param outputPolicy strategy describe how to output some information
    * @param neighbourRender allow to render or not neighbour
    * @param perfomance the perfomance of program context
    * @return
    */
  def apply(
             worldInitializer: ScafiWorldInitializer,
             scafiSimulationInfo : SimulationInfo,
             simulationInitializer: ScafiSimulationInitializer,
             scafiWorldInfo : ScafiWorldInformation = ScafiWorldInformation.standard,
             commandMapping: CommandBinding = StandardBinding,
             outputPolicy: OutputPolicy = StandardFXOutput,
             neighbourRender: Boolean = false,
             log : LogConfiguration = LogConfiguration.GraphicsLog,
             perfomance: PerformancePolicy = StandardPolicy): Program[_,_] = {
    new ScafiProgramBuilder(new ScafiConfiguration(scafiWorldInfo,
      worldInitializer,
      commandMapping,
      scafiSimulationInfo,
      simulationInitializer,
      outputPolicy,
      neighbourRender,
      log,
      perfomance)).create
  }

  /**
    * create a program builder passing the configuration craeted
    * @param configuration the program configuration
    * @return the program create
    */
  def apply(configuration : ScafiConfiguration) : Program[_,_] = new ScafiProgramBuilder(configuration).create
}