package it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration

import it.unibo.scafi.simulation.frontend.configuration.command.factory._
import it.unibo.scafi.simulation.frontend.configuration.logger.LogConfiguration._
import it.unibo.scafi.simulation.frontend.configuration.parser.{Parser, UnixLikeParser}
import it.unibo.scafi.simulation.frontend.controller.input.InputCommandController
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.scafiSimulationExecutor
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.command._
import it.unibo.scafi.simulation.frontend.view.OutputPolicy.NoOutput
import it.unibo.scafi.simulation.frontend.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.frontend.view.scalaFX.drawer.{FastFXOutput, StandardFXOutput}

/**
  * describe the set of information used to create virtual machine with command
  * supported by scafi application
  */
object ScafiInformation {
  implicit val configurationBuilder : ScafiConfigurationBuilder = new ScafiConfigurationBuilder
  lazy val configuration = configurationBuilder.create().get

  /**
    * parser unix like used to parse string value in configuration command
    */
  val UnixConfiguration : Parser[String] = new UnixLikeParser(new ListDemoCommandFactory,
    new RandomWorldCommandFactory,
    new GridWorldCommandFactory,
    new RadiusSimulationCommandFactory,
    new LaunchCommandFactory,
    ExitCommandFactory,
    new LanguageCommandFactory,
    new PerformanceCommandFactory,
    new GraphicsLogCommandFactory,
    new OutputCommandFactory(FastFXOutput,StandardFXOutput,NoOutput),
    new WindowConfigurationCommandFactory,
    new LogCommandFactory(NoLog,StandardLog,GraphicsLog),
    new RenderCommandFactory)
  /**
    * parser unix like used to parse string value in runtime command
    */
  val UnixRuntime : Parser[String] = new UnixLikeParser(new SimulationCommandFactory(scafiSimulationExecutor),
    new SingleMoveCommandFactory,
    new SingleToggleCommandFactory,
    ExitCommandFactory,
    LookExportCommandFactory,
    InputCommandController.UndoCommandFactory,
    ShowIdCommandFactory)
}
