package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArg, CommandArgDescription, IntType}
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Grid
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * a factory used to create a command used to set the world initializer as grid initializer
  * @param scafiConfiguration the scafi configuration builder
  */
class GridWorldCommandFactory(implicit val scafiConfiguration: ScafiConfigurationBuilder) extends CommandFactory {
  import GridWorldCommandFactory._
  import CommandFactory._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  override val name: String = "grid-world"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Space,IntType,description = international(name, Space)),
      CommandArgDescription(Column,IntType,description = international(name, Space)),
      CommandArgDescription(Row,IntType,description = international(name, Space)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    //command argument used to create command
    var space : Option[Int] = None
    var column : Option[Int] = None
    var row : Option[Int] = None

    //verify if the space is set in the argument passed and if the type if valid
    args.get(Space) match {
      case Some(value : Int) => space = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Space)))
      case _ =>
    }

    //verify if the column is set in the argument passed and if the type if valid
    args.get(Column) match {
      case Some(value : Int) => column = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Column)))
      case _ =>
    }

    //verify if the row is set in the argument passed and if the type if valid
    args.get(Row) match {
      case Some(value : Int) => row = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Row)))
      case _ =>
    }

    if(space.isDefined && column.isDefined && row.isDefined) {
      //create the command
      creationSuccessful(onlyMakeCommand(() => {
        scafiConfiguration.worldInitializer = Some(Grid(space.get,column.get,row.get))
        Success
      }))
    } else {
      //if the value name is different return a failed result
      creationFailed(Fail(CommandFactory.wrongParameterName(Space, Column, Row)))
    }
  }
}

object GridWorldCommandFactory {
  val Space = "space"
  val Column = "column"
  val Row = "row"
}
