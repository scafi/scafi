package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.{FastPerformancePolicy, PerformancePolicy}
import it.unibo.scafi.simulation.gui.configuration.{CommandMapping, ViewEnvironment}
import it.unibo.scafi.simulation.gui.controller.presenter.SimulationPresenter
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiLikeWorld, scafiWorld, ScafiWorldInitializer}
import it.unibo.scafi.simulation.gui.incarnation.scafi.{ScafiCommandMapping, ScafiProgramEnvironment}
import it.unibo.scafi.simulation.gui.launcher.MetaLauncher
import it.unibo.scafi.simulation.gui.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutputPolicy}
import it.unibo.scafi.simulation.gui.view.{OutputPolicy, SimulationView}

/**
  * a builder used to create a scafi simulation
  */
object ScafiProgramBuilder {
  def apply(worldInitializer: ScafiWorldInitializer,
            commandMapping: CommandMapping = ScafiCommandMapping.standardMapping,
            simulation: ScafiSimulation,
            outputPolicy: OutputPolicy = StandardFXOutputPolicy,
            neighbourRender: Boolean = false,
            perfomance: PerformancePolicy = FastPerformancePolicy): MetaLauncher[ScafiLikeWorld,SimulationView] = {
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
    worldInitializer.init()
    val bridged = simulation.create
    new MetaLauncher[ScafiLikeWorld,SimulationView](new ScafiProgramEnvironment(presenter,bridged,perfomance),env,commandMapping)
  }
}