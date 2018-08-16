package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.GridCommandFactory.GridArg
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Grid
import it.unibo.scafi.simulation.gui.util.Result.Success

/**
  * a command factory used to create command that set a grid initializer
  * @param scafiConfiguration the configuration used to save the command result
  */
class GridCommandFactory(scafiConfiguration: ScafiConfiguration) extends CommandFactory {

  override def name: CommandName = CommandFactory.CommandFactoryName.Grid

  override def create(arg: CommandFactory.CommandArg): Option[Command] = arg match {
    case GridArg(space,row,column) => Some(onlyMakeCommand{() => {
      scafiConfiguration.worldInitializer = Some(Grid(space,row,column))
      Success
    }})
    case _ => None
  }
}

object GridCommandFactory {
  private val regex = raw"initializer=grid\((\d+)\,(\d+)\,(\d+)\)".r

  /**
    * grid command argument used to create a grid initializer
    * @param space the space between two node
    * @param row the number of row
    * @param column the number of colum
    */
  case class GridArg(space : Int, row : Int, column : Int) extends CommandArg
  object GridStringParser extends StringCommandParser  {
    override def parse: Option[CommandFactory.CommandArg] = arg match {
      case regex(space,row,column) => {
        Some(GridArg(space.toInt,row.toInt,column.toInt))
      }
      case _ => None
    }
    override def help: String = "type initializer=grid(space,column,row) to create a grid initializer"
  }
}
