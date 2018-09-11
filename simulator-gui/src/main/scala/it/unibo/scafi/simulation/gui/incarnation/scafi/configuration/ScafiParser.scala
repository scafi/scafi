package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.factory._
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration._
import it.unibo.scafi.simulation.gui.configuration.parser.{Parser, UnixLikeParser}
import it.unibo.scafi.simulation.gui.controller.input.InputCommandController
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationExecutor
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command._
import it.unibo.scafi.simulation.gui.view.OutputPolicy.NoOutput
import it.unibo.scafi.simulation.gui.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutput, StandardFXOutput}

/**
  * describe the set of information used to create virtual machine with command
  * supported by scafi application
  */
object ScafiParser {
  implicit val scafiConfiguration : ScafiConfigurationBuilder = new ScafiConfigurationBuilder
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
    new WindowConfigurationCommandFactory(ScalaFXEnvironment),
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
