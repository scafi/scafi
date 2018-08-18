package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.RadiusCommandFactory.RadiusArg
import it.unibo.scafi.simulation.gui.util.Result.Success

/**
  * a command factory used to create a command that initialize a simulation with some neighbour radius
  * @param scafiConfiguration the the configuration used to save the command result
  */
class RadiusCommandFactory(scafiConfiguration: ScafiConfiguration) extends CommandFactory{
  override def name: CommandName = CommandFactory.CommandFactoryName.Radius

  override def create(arg: CommandFactory.CommandArg): Option[Command] = arg match {
    case RadiusArg(radius) => Some(onlyMakeCommand{
      () => {
        scafiConfiguration.scafiSimulationInitializer = Some(RadiusSimulationInitializer(radius))
        Success
      }
    })
    case _ => None
  }
}

object RadiusCommandFactory {
  private val regex = raw"simulation=radius\((\d+)\)".r

  /**
    * the argument used to create a radius command
    * @param radius the radius
    */
  case class RadiusArg(radius : Double) extends CommandArg

  /**
    * a radius string parser used to parse a string into a radius argument
    */
  object RadiusStringParser extends StringCommandParser  {
    override def parse(arg : String): Option[CommandFactory.CommandArg] = arg match {
      case regex(radius) => Some(RadiusArg(radius.toInt))
      case  _ => None
    }

    override def help: String = "use simulation=radius(neighbourRange) to create a radius simulation"
  }
}
