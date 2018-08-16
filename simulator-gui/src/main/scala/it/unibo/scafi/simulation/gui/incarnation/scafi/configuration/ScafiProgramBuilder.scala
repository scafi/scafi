package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.{PerformancePolicy, StandardPolicy}
import it.unibo.scafi.simulation.gui.configuration.ViewEnvironment
import it.unibo.scafi.simulation.gui.configuration.command.CommandMapping
import it.unibo.scafi.simulation.gui.controller.presenter.SimulationPresenter
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{ScafiSimulationInitializer, ScafiSimulationSeed}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiLikeWorld, ScafiWorldInitializer, scafiWorld}
import it.unibo.scafi.simulation.gui.incarnation.scafi.{ScafiCommandMapping, ScafiProgramEnvironment}
import it.unibo.scafi.simulation.gui.launcher.MetaLauncher
import it.unibo.scafi.simulation.gui.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutputPolicy}
import it.unibo.scafi.simulation.gui.view.{OutputPolicy, SimulationView}

/**
  * a builder used to create a scafi simulation
  */
object ScafiProgramBuilder {
  /**
    * allow to create a meta launcher passing some parameter
    * @param scafiSeed the world seed in scafi context
    * @param worldInitializer a world initializer to initialize scafi world
    * @param commandMapping a command mapping used to map some keyboard (and selection) input to an action
    * @param scafiSimulationSeed a seed used to set some parameter in scafi bridge simulation
    * @param simulationInitializer a simulation initializer used to initialize scafi bridge simulation
    * @param outputPolicy strategy describe how to output some information
    * @param neighbourRender allow to render or not neighbour
    * @param perfomance the perfomance of program context
    * @return
    */
  def apply(scafiSeed : ScafiSeed = ScafiSeed.standard,
            worldInitializer: ScafiWorldInitializer,
            commandMapping: CommandMapping = ScafiCommandMapping.standardMapping,
            scafiSimulationSeed : ScafiSimulationSeed,
            simulationInitializer: ScafiSimulationInitializer,
            outputPolicy: OutputPolicy = StandardFXOutputPolicy,
            neighbourRender: Boolean = false,
            perfomance: PerformancePolicy = StandardPolicy): MetaLauncher[ScafiLikeWorld,SimulationView] = {
    //standard presenter
    val presenter = new SimulationPresenter[ScafiLikeWorld](scafiWorld,neighbourRender)
    //check if outputpolicy is supported
    val env : ViewEnvironment[SimulationView] = outputPolicy match {
      case policy : FXOutputPolicy => {
        ScalaFXEnvironment.drawer = policy
        ScalaFXEnvironment
      }
      case _ => throw new IllegalArgumentException("output policy don't supported")
    }
    //init the world
    worldInitializer.init(scafiSeed)
    val bridged = simulationInitializer.create(scafiSimulationSeed)
    new MetaLauncher[ScafiLikeWorld,SimulationView](new ScafiProgramEnvironment(presenter,bridged,perfomance),env,commandMapping)
  }
}