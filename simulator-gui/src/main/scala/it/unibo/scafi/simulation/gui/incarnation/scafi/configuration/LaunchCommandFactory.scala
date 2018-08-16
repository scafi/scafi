package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.EmptyArg
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.{Demo, SimulationProfile}
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy

/**
  * a command factory used to create a command that launch a scafi program
  * @param scafiConfiguration the configuration used to launch the program
  */
class LaunchCommandFactory(scafiConfiguration: ScafiConfiguration) extends CommandFactory {
  override def name: CommandName = CommandFactory.CommandFactoryName.Launch

  override def create(arg: CommandFactory.CommandArg): Option[Command] = arg match {
    case EmptyArg => {
      Some(onlyMakeCommand( () => {
        if(scafiConfiguration.demo.isEmpty) {
          Fail(LaunchCommandFactory.DemoEmpty)
        } else if(scafiConfiguration.worldInitializer.isEmpty) {
          Fail(LaunchCommandFactory.InitializerEmpty)
        } else if(scafiConfiguration.scafiSimulationInitializer.isEmpty) {
          Fail(LaunchCommandFactory.SimulationEmpty)
        } else {
          val annotation : Demo = scafiConfiguration.demo.get.getAnnotation(classOf[Demo])
          val profile : SimulationProfile = annotation.simulationType.profile
          ScafiProgramBuilder (
            worldInitializer = scafiConfiguration.worldInitializer.get,
            scafiSeed = scafiConfiguration.worldSeed.get,
            scafiSimulationSeed = ScafiSimulationSeed(program = scafiConfiguration.demo.get, action = profile.action),
            simulationInitializer = scafiConfiguration.scafiSimulationInitializer.get,
            outputPolicy = StandardFXOutputPolicy,
            neighbourRender = true,
            perfomance = NearRealTimePolicy,
            commandMapping = profile.commandMapping
          ).launch()
          scafiConfiguration.launched=true
          Success
        }
      }))
    }
    case _ => None
  }
}

object LaunchCommandFactory {
  private [LaunchCommandFactory] val DemoEmpty = "demo isn't set"
  private [LaunchCommandFactory] val InitializerEmpty = "world initiliazer isn't set"
  private [LaunchCommandFactory] val SimulationEmpty = "simulation empty"

  /**
    * a string parser used to create a launch command
    */
  object LaunchStringParser extends StringCommandParser {

    override def parse: Option[CommandFactory.CommandArg] = if(arg == "launch") Some(EmptyArg) else None

    override def help: String = "use launch to launch application"
  }
}