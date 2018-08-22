package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.command
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * abstract factory class used to create a move command
  * @param world the world where command is executed
  */
abstract class AbstractMoveCommandFactory(val world: AggregateWorld) extends CommandFactory {
  /**
    * allow to create move command
    * @param movedMap the node map that associated an id to a new position
    * @return the command created
    */
  protected def move(movedMap : Map[world.ID,world.P]) : Command = {
    //create old position map for the unmake method into command
    val oldMap = movedMap filter { x => world(x._1).isDefined} map {x => x._1 -> world(x._1).get.position}
    command( () => {
      movedMap foreach{ x => world.moveNode(x._1,x._2)}
      Success
    })( () => {
      oldMap foreach{ x => world.moveNode(x._1,x._2)}
      Success
    })
  }
}

object AbstractMoveCommandFactory {
  import CommandFactory._
  val MoveMap = "map"

  /**
    * a factory that create a move command that move a set of node
    * @param world the world where command is executed
    * @param analyzer the analyzer used to check the correctness of world type at runtime
    */
  class MultiMoveCommandFactory(override val world : AggregateWorld)(implicit val analyzer: WorldTypeAnalyzer)
    extends AbstractMoveCommandFactory(world) {

    override def name: String = "move-multi"

    override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
      List(CommandArgDescription(MoveMap,MapValue(AnyType,AnyType)))

    override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(MoveMap) match {
      case Some(nodeToMove : Map[_,_]) => if(nodeToMove.keySet.forall(analyzer.acceptId(_)) && nodeToMove.values.forall(_.isInstanceOf[world.P])) {
        val nodeToPosition = nodeToMove.map {x => x._1.asInstanceOf[world.ID] -> x._2.asInstanceOf[world.P]}
        creationSuccessful(move(nodeToPosition))
      } else {
        creationFailed(Fail(wrongTypeParameter(MapValue(AnyType,AnyType),MoveMap)))
      }
      case _ => creationFailed(Fail(wrongParameterName(MoveMap)))
    }
  }
}