package it.unibo.scafi.simulation.frontend.incarnation.scafi

import it.unibo.scafi.simulation.frontend.configuration.environment.ProgramEnvironment
import it.unibo.scafi.simulation.frontend.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.frontend.controller.input.{InputCommandController, InputController}
import it.unibo.scafi.simulation.frontend.controller.logical.{ExternalSimulation, LogicController}
import it.unibo.scafi.simulation.frontend.controller.presenter.Presenter
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.{ScafiLikeWorld, scafiWorld}
import it.unibo.scafi.simulation.frontend.view.SimulationView

/**
  * scafi environment used to configure scafi application
  * @param presenter the presenter of scafi simulation
  * @param simulation the simulation
  * @param policy the policy
  * @param controller the controller
  */
class ScafiProgramEnvironment(val presenter : Presenter[ScafiLikeWorld,SimulationView],
                              val simulation : ExternalSimulation[ScafiLikeWorld],
                              val policy : ProgramEnvironment.PerformancePolicy,
                              val logConfiguration : LogConfiguration,
                              val controller : LogicController[ScafiLikeWorld]*)
                              extends ProgramEnvironment[ScafiLikeWorld,SimulationView] {

  override val input: InputController = InputCommandController

  override val world: ScafiLikeWorld = scafiWorld
}