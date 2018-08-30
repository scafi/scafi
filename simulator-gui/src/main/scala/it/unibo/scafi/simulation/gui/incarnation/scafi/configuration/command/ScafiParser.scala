package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.factory._
import it.unibo.scafi.simulation.gui.configuration.parser.{Parser, UnixLikeParser}
import it.unibo.scafi.simulation.gui.controller.input.InputCommandController
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationObserver
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutputPolicy, StandardFXOutputPolicy}

/**
  * describe the scafi parser
  */
object ScafiParser {
  implicit val scafiConfiguration = new ScafiConfigurationBuilder
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
    new OutputCommandFactory(FastFXOutputPolicy,StandardFXOutputPolicy),
    new WindowConfigurationCommandFactory(ScalaFXEnvironment))
  /**
    * parser unix like used to parse string value in runtime command
    */
  val UnixRuntime : Parser[String] = new UnixLikeParser(new SimulationCommandFactory(scafiSimulationObserver),
    new SingleMoveCommandFactory,
    new SingleToggleCommandFactory,
    ExitCommandFactory,
    InputCommandController.UndoCommandFactory)
}
