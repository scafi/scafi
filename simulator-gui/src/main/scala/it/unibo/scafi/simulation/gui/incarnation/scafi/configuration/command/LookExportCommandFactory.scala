package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationExecutor
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Fail

/**
  * a factory that create command used to look export produced by a node 
  */
object LookExportCommandFactory extends CommandFactory{
  val Id = "id"
  import CommandFactory._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  override def name: String = "observe-export"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Id,IntType,description = international(name,Id)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(Id) match {
    case Some(id : Int) => easyResultCreation(() =>scafiSimulationExecutor.observeExport(id))
    case Some(_) => creationFailed(Fail(wrongTypeParameter(IntType,Id)))
    case _ => creationFailed(Fail(wrongParameterName(Id)))
  }
}

