package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail
import it.unibo.scafi.space.Point3D

/**
  * a factory that allow to create a move command that move only a node
  */
class SingleMoveCommandFactory extends AbstractMoveCommandFactory {
  import CommandFactory._
  import SingleMoveCommandFactory._
  import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
  override val name: String = "move"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Id,IntType,description = international(name, Id)),
      CommandArgDescription(X,IntType,description = international(name, X)),
      CommandArgDescription(Y,IntType,description = international(name, Y)),
      CommandArgDescription(Z,IntType,optional = true,international(name, Z)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    var id : Option[Int] = None
    var x : Option[Int] = None
    var y : Option[Int] = None
    var z : Option[Double] = None

    args.get(Id) match {
      case Some(idValue : Int) => id = Some(idValue)
      case Some(_) => creationFailed(Fail(wrongTypeParameter(IntType,Id)))
      case _ =>
    }

    args.get(X) match {
      case Some(xValue : Int) => x = Some(xValue)
      case Some(_) => creationFailed(Fail(wrongTypeParameter(IntType,X)))
      case _ =>
    }

    args.get(Y) match {
      case Some(yValue: Int) => y = Some(yValue)
      case Some(_) => creationFailed(Fail(wrongTypeParameter(IntType, Y)))
      case _ =>
    }

    args.get(Z) match {
      case Some(zValue : Int) => z = Some(zValue)
      case Some(_) => creationFailed(Fail(wrongTypeParameter(IntType,Z)))
      case _ =>
    }

    if(id.isDefined && x.isDefined && y.isDefined) {
      val newPos = Map(id.get -> Point3D(x.get,y.get,z.getOrElse(0.0)))
      creationSuccessful(move(newPos))
    } else {
      creationFailed(Fail(wrongParameterName(Id, X, Y)))
    }
  }
}

object SingleMoveCommandFactory {
  val Id = "id"
  val X = "x"
  val Y = "y"
  val Z = "z"
}