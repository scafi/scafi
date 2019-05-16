package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command.command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.scafiSimulationExecutor
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.{Fail, Success}
import it.unibo.scafi.space.Point3D

/**
  * abstract factory class used to create a move command

  */
abstract class AbstractMoveCommandFactory extends CommandFactory {
  /**
    * allow to create move command
    * @param movedMap the node map that associated an id to a new position
    * @return the command created
    */
  protected def move(movedMap : Map[ID,P]) : Command = {
    import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiBridge._
    val bridged = scafiSimulationExecutor.contract.simulation.get
    //create old position map for the unmake method into command
    val oldMap = movedMap map {x => x._1 -> scafiWorld(x._1).get.position}
    command( () => {
      bridged.add(bridged.MultiNodeMovement(movedMap))
      Success
    })( () => {
      bridged.add(bridged.MultiNodeMovement(oldMap))
      Success
    })
  }
}

object AbstractMoveCommandFactory {
  import CommandFactory._
  val MoveMap = "map"

  /**
    * a factory that create a move command that move a set of node
    */
  class MultiMoveCommandFactory
    extends AbstractMoveCommandFactory {
    import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiBridge._
    override def name: String = "move-multi"

    override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
      List(CommandArgDescription(MoveMap,MapValue(AnyType,AnyType)))

    override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(MoveMap) match {
      case Some(nodeToMove : Map[_,_]) =>
        if(nodeToMove.keySet.forall(_.isInstanceOf[ID]) && nodeToMove.values.forall(_.isInstanceOf[Point3D])) {
        val nodeToPosition = nodeToMove.map {x => x._1.asInstanceOf[ID] -> x._2.asInstanceOf[Point3D]}
        creationSuccessful(move(nodeToPosition))
      } else {
        creationFailed(Fail(wrongTypeParameter(MapValue(AnyType,AnyType),MoveMap)))
      }
      case _ => creationFailed(Fail(wrongParameterName(MoveMap)))
    }
  }
}