package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.command
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.MoveCommandFactory.MoveArg
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.space.Point
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Success

/**
  * a factory used to create a command that move an id set
  * @param world the world where the node moved
  * @param analyzer type analyzer used to check the correctness of world type at runtime
  */
class MoveCommandFactory(private val world : AggregateWorld)(implicit val analyzer : WorldTypeAnalyzer) extends CommandFactory{

  override val name: CommandName = CommandFactory.CommandFactoryName.Move

  override def create(arg: CommandArg): Option[Command] = arg match {
    case MoveArg(map) => if(map.keySet.forall(analyzer.acceptId(_)) && map.values.forall(_.isInstanceOf[Point])) {
      val oldPos : Map[world.ID,world.P] = world.nodes map {x => x.id -> x.position} toMap
      val mapped : Map[world.ID,world.P] = map.map {x => x._1.asInstanceOf[world.ID] -> x._2.asInstanceOf[world.P]}
      Some(command(() => move(mapped))(() => move(oldPos)))
    } else {
      None
    }

    case _ => None
  }

  private def move (move : Map[world.ID,world.P]): Result = {
    move filter {x => world(x._1).isDefined} foreach {x => world.moveNode(x._1,x._2)}
    Success
  }
}

object MoveCommandFactory {
  /**
    * move arg used to create a move command
    * @param map associated ad id to a position
    */
  case class MoveArg(map : Map[Any,Any]) extends CommandArg
}