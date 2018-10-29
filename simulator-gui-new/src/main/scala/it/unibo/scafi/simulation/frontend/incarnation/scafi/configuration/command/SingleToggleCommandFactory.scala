package it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.frontend.configuration.command._
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.{ScafiLikeWorld, scafiWorld}
import it.unibo.scafi.simulation.frontend.util.Result
import it.unibo.scafi.simulation.frontend.util.Result.Fail

/**
  * a factory that create a command to toggle a single node
  */
class SingleToggleCommandFactory extends AbstractToggleCommandFactory {
  import CommandFactory._
  import SingleToggleCommandFactory._
  import it.unibo.scafi.simulation.frontend.configuration.launguage.ResourceBundleManager._
  override val name: String = "toggle"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Id,IntType,description = international(name, Id)),
      CommandArgDescription(Name,StringType,description = international(name, Name)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    //parameter used to create command
    var id : Option[Int] = None
    var name : Option[String] = None

    //verify if the id is set and if the id type is correct
    args.get(Id) match {
      case Some(idValue : Int) => id = Some(idValue)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Id)))
      case _ =>
    }

    //verify if the name is set and if the name type is correct
    args.get(Name) match {
      case Some(nameValue : String) => name = Some(nameValue)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(StringType,Name)))
      case _ =>
    }

    if(id.isDefined && name.isDefined) {
      creationSuccessful(toggle(name.get,List(id.get)))
    } else {
      creationFailed(Fail(wrongParameterName(Id, Name)))
    }
  }
}

object SingleToggleCommandFactory {
  val Id = "id"
  val Name = "name"
}
