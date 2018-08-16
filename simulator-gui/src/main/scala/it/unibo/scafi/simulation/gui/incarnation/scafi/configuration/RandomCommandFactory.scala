package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.RandomCommandFactory.RandomArg
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.util.Result.Success

/**
  * a command factory used to create a command used to create a random world initializer
  * @param scafiConfiguration the configuration used to save the command result
  */
class RandomCommandFactory(scafiConfiguration: ScafiConfiguration) extends CommandFactory {
  /**
    * @return the name of factory
    */
  override def name: CommandName = CommandFactory.CommandFactoryName.Random

  /**
    * create a command with the command args
    *
    * @param arg the command arg
    * @return None if the arg is not legit some of command otherwise
    */
  override def create(arg: CommandFactory.CommandArg): Option[Command] = arg match {
    case RandomArg(node,width,height) => Some(onlyMakeCommand{() => {
      scafiConfiguration.worldInitializer = Some(Random(node,width,height))
      Success
    }})
    case _ => None
  }

}

object RandomCommandFactory {
  private val regex = raw"initializer=random\((\d+),(\d+),(\d+)\)".r

  /**
    * the argument used to create a random world initializer
    * @param node number of node in the world
    * @param width the world width
    * @param height the world height
    */
  case class RandomArg(node : Int, width : Int, height : Int) extends CommandArg

  /**
    * random string parser is used to parse a string into a random command argument
    */
  object RandomStringParser extends StringCommandParser  {

    override def parse: Option[CommandFactory.CommandArg] = arg match {
      case regex(node,width,height) => Some(RandomArg(node.toInt,width.toInt,height.toInt))
      case _ => None
    }

    override def help: String = "type initializer=random(node,width,height) to create a random initializer"
  }
}