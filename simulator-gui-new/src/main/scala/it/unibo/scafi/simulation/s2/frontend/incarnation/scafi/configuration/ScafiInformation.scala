package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration

import it.unibo.scafi.simulation.s2.frontend.configuration.command.factory._
import it.unibo.scafi.simulation.s2.frontend.configuration.logger.LogConfiguration._
import it.unibo.scafi.simulation.s2.frontend.configuration.parser.Parser
import it.unibo.scafi.simulation.s2.frontend.configuration.parser.UnixLikeParser
import it.unibo.scafi.simulation.s2.frontend.controller.input.InputCommandController
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationExecutor
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command._
import it.unibo.scafi.simulation.s2.frontend.view.OutputPolicy.NoOutput
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.FastFXOutput
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.StandardFXOutput

/**
 * describe the set of information used to create virtual machine with command supported by scafi application
 */
object ScafiInformation {
  implicit val configurationBuilder: ScafiConfigurationBuilder = new ScafiConfigurationBuilder
  lazy val configuration: ScafiConfiguration = configurationBuilder.create().get

  /**
   * parser unix like used to parse string value in configuration command
   */
  val UnixConfiguration: Parser[String] = new UnixLikeParser(
    new ListDemoCommandFactory,
    new RandomWorldCommandFactory,
    new GridWorldCommandFactory,
    new RadiusSimulationCommandFactory,
    new LaunchCommandFactory,
    ExitCommandFactory,
    new LanguageCommandFactory,
    new PerformanceCommandFactory,
    new GraphicsLogCommandFactory,
    new OutputCommandFactory(FastFXOutput, StandardFXOutput, NoOutput),
    new WindowConfigurationCommandFactory,
    new LogCommandFactory(NoLog, StandardLog, GraphicsLog),
    new RenderCommandFactory
  )
  /**
   * parser unix like used to parse string value in runtime command
   */
  val UnixRuntime: Parser[String] = new UnixLikeParser(
    new SimulationCommandFactory(ScafiSimulationExecutor),
    new SingleMoveCommandFactory,
    new SingleToggleCommandFactory,
    ExitCommandFactory,
    LookExportCommandFactory,
    InputCommandController.UndoCommandFactory,
    ShowIdCommandFactory
  )
}
