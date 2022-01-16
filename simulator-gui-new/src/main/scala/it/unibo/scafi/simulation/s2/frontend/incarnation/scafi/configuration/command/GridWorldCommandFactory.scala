package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Grid
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail

/**
 * a factory used to create a command used to set the world initializer as grid initializer
 * @param scafiConfiguration
 *   the scafi configuration builder
 */
class GridWorldCommandFactory(implicit val scafiConfiguration: ScafiConfigurationBuilder) extends CommandFactory {
  import CommandFactory._
  import GridWorldCommandFactory._
  import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
  override val name: String = "grid-world"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(
      CommandArgDescription(Space, IntType, description = international(name, Space)),
      CommandArgDescription(Column, IntType, description = international(name, Space)),
      CommandArgDescription(Row, IntType, description = international(name, Space))
    )

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    // command argument used to create command
    var space: Option[Int] = None
    var column: Option[Int] = None
    var row: Option[Int] = None

    // verify if the space is set in the argument passed and if the type if valid
    args.get(Space) match {
      case Some(value: Int) => space = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType, Space)))
      case _ =>
    }

    // verify if the column is set in the argument passed and if the type if valid
    args.get(Column) match {
      case Some(value: Int) => column = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType, Column)))
      case _ =>
    }

    // verify if the row is set in the argument passed and if the type if valid
    args.get(Row) match {
      case Some(value: Int) => row = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType, Row)))
      case _ =>
    }

    if (space.isDefined && column.isDefined && row.isDefined) {
      easyResultCreation(() => scafiConfiguration.worldInitializer = Some(Grid(space.get, column.get, row.get)))
    } else {
      // if the value name is different return a failed result
      creationFailed(Fail(CommandFactory.wrongParameterName(Space, Column, Row)))
    }
  }
}

object GridWorldCommandFactory {
  val Space = "space"
  val Column = "column"
  val Row = "row"
}
