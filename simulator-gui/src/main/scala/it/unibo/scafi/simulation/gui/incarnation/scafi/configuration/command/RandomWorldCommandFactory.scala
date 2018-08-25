package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArg, CommandArgDescription, IntType}
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * a factory used to create a command used to set the world initializer as random initializer
  * @param scafiConfiguration the scafi configuration builder
  */
class RandomWorldCommandFactory(implicit val scafiConfiguration: ScafiConfigurationBuilder) extends CommandFactory {
  import RandomWorldCommandFactory._
  import CommandFactory._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  override val name: String = "random-world"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Node,IntType,description = international(name, Node)),
      CommandArgDescription(Width,IntType, description = international(name, Width)),
      CommandArgDescription(Height,IntType, description = international(name, Height)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    //the command arguments used to create command
    var node : Option[Int] = None
    var width : Option[Int] = None
    var height : Option[Int] = None
    //verify if the node is set in the argument passed and if the type if valid
    args.get(Node) match {
      case Some(value : Int) => node = Some(value)
      case Some(_) => return creationFailed(Fail(CommandFactory.wrongTypeParameter(IntType,Node)))
      case _ =>
    }

    //verify if the width is set in the argument passed and if the type if valid
    args.get(Width) match {
      case Some(value : Int) => width = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Width)))
      case _ =>
    }

    //verify if the height is set in the argument passed and if the type if valid
    args.get(Height) match {
      case Some(value : Int) => height = Some(value)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Height)))
      case _ =>
    }

    if(width.isDefined && height.isDefined && node.isDefined) {
      //create the command
      creationSuccessful(onlyMakeCommand(() => {
        scafiConfiguration.worldInitializer = Some(Random(node.get,width.get,height.get))
        Success
      }))
    } else {
      //if the value name is different return a failed result
      creationFailed(Fail(wrongParameterName(Node , Width, Height)))
    }
  }
}

object RandomWorldCommandFactory {
  val Node = "node"
  val Width = "width"
  val Height = "height"
}