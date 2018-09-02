package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.gui.controller.input.{InputCommandController, InputController}
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logical.{ExternalSimulation, LogicController}
import it.unibo.scafi.simulation.gui.controller.presenter.Presenter
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.view.scalaFX.logger.FXLogger
import it.unibo.scafi.simulation.gui.view.{SimulationView, WindowConfiguration, scalaFX}

/**
  * scafi enviroment
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
}

object ScafiProgramEnvironment {
  val scafiStandardLog = new LogConfiguration {
    override def apply(): Unit = {
      LogConfiguration.standardLog()
      scalaFX.initializeScalaFXPlatform
      FXLogger.acceptChannel(LogManager.acceptAll)
      LogManager.attach(FXLogger)
    }
  }
}
