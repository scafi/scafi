package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.{MoveCommandFactory, SimulationCommandFactory, ToggleCommandFactory}
import it.unibo.scafi.simulation.gui.configuration.command.SimulationCommandFactory.SimulationStringParser
import it.unibo.scafi.simulation.gui.configuration.language.{ConfigurationLanguage, OnlineLanguage}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationObserver
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.DemoCommandFactory.DemoStringParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.GridCommandFactory.GridStringParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.LaunchCommandFactory.LaunchStringParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.RadiusCommandFactory.RadiusStringParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.RandomCommandFactory.RandomStringParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.WorldSeedCommandFactory.WorldSeedStringParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.WorldStringParser.{MoveStringParser, ToggleStringParser}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.scafiWorld

object ScafiLanguage{
  val config = new ScafiConfiguration()
  /**
    * string used to launch scafi program
    */
  val launch = "launch"
  /**
    * configuration language used to create a
    */
  val configurationLanguage = new ConfigurationLanguage((Map(
    DemoStringParser -> new DemoCommandFactory(config),
    LaunchStringParser -> new LaunchCommandFactory(config),
    RandomStringParser -> new RandomCommandFactory(config),
    GridStringParser -> new GridCommandFactory(config),
    RadiusStringParser -> new RadiusCommandFactory(config),
    WorldSeedStringParser -> new WorldSeedCommandFactory(config))))
  import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld._
  val runtimeLanguage = new OnlineLanguage(Map(
    SimulationStringParser -> new SimulationCommandFactory(scafiSimulationObserver),
    ToggleStringParser -> new ToggleCommandFactory(scafiWorld),
    MoveStringParser -> new MoveCommandFactory(scafiWorld)
  ))
}
