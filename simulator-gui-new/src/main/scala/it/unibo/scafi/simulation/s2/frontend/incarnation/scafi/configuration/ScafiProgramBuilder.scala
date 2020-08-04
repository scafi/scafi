package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration

import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandBinding
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ProgramEnvironment.{PerformancePolicy, StandardPolicy}
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.s2.frontend.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.s2.frontend.configuration.{Program, ProgramBuilder}
import it.unibo.scafi.simulation.s2.frontend.controller.presenter.SimulationPresenter
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.ScafiCommandBinding.StandardBinding
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.ScafiProgramEnvironment
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.{ScafiBridge, ScafiSimulationInitializer, SimulationInfo}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.{ScafiLikeWorld, ScafiWorldInitializer, scafiWorld}
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.StandardFXOutput
import it.unibo.scafi.simulation.s2.frontend.view.{OutputPolicy, SimulationView, ViewSetting}

/*
 * scafi program builder used to create scafi program
 * @param configuration the scafi configuration used to initialize program
 */
private class ScafiProgramBuilder(override val configuration: ScafiConfiguration) extends ProgramBuilder[ScafiConfiguration] {
  override def create: Program[_,_] = {
    val presenter = new SimulationPresenter[ScafiLikeWorld](scafiWorld,configuration.neighbourRender)
    //get the current view environment, attach the drawer passed to configuration
    val viewEnv : Option[ViewEnvironment[SimulationView]] = configuration.outputPolicy.getViewEnvAndAttach()
    //set name, logo and icon
    ViewSetting.windowConfiguration = ScafiWindowInfo(ViewSetting.windowConfiguration)
    //init the world
    configuration.worldInitializer.init(configuration.scafiWorldInfo)
    val bridged = configuration.simulationInitializer.create(configuration.scafiSimulationInformation)
    val programEnv = new ScafiProgramEnvironment(presenter,bridged,configuration.performance,configuration.logConfiguration)
    ScafiBridge.Instance = Some(bridged)
    new Program[ScafiLikeWorld,SimulationView](programEnv, viewEnv,configuration.commandMapping)
  }
}
object ScafiProgramBuilder {
  /**
    * allow to create a program passing some parameter
    * you can launch a scafi aggregate application like this:
    * <pre>
    *   {@code
         ScafiProgramBuilder (
            Random(500,500,500),
            SimulationInfo(program = classOf[Simple]),
            RadiusSimulation(radius = 40)
          ).launch()
    *   }
    * </pre>
    * @param scafiWorldInfo the world seed in scafi context
    * @param worldInitializer a world initializer to initialize scafi world
    * @param commandMapping a command mapping used to map some keyboard (and selection) input to an action
    * @param scafiSimulationInfo a seed used to set some parameter in scafi bridge simulation
    * @param simulationInitializer a simulation initializer used to initialize scafi bridge simulation
    * @param outputPolicy strategy describe how to output some information
    * @param neighbourRender allow to render or not neighbour
    * @param performance the perfomance of program context
    * @return the program create by information passed
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
             performance: PerformancePolicy = StandardPolicy): Program[_,_] = {
    ScafiInformation.configurationBuilder.neighbourRender = neighbourRender
    ScafiInformation.configurationBuilder.logConfiguration = log
    ScafiInformation.configurationBuilder.commandMapping = commandMapping
    ScafiInformation.configurationBuilder.scafiSimulationInformation = Some(scafiSimulationInfo)
    ScafiInformation.configurationBuilder.worldInitializer = Some(worldInitializer)
    ScafiInformation.configurationBuilder.scafiWorldInfo = scafiWorldInfo
    ScafiInformation.configurationBuilder.simulationInitializer = Some(simulationInitializer)
    ScafiInformation.configurationBuilder.outputPolicy = outputPolicy
    ScafiInformation.configurationBuilder.performance = performance
    new ScafiProgramBuilder(ScafiInformation.configurationBuilder.create().get).create
  }

  /**
    * create a program builder passing the configuration created
    * @param configuration the program configuration
    * @return the program create
    */
  def apply(configuration : ScafiConfiguration) : Program[_,_] = new ScafiProgramBuilder(configuration).create
}