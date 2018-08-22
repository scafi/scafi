package it.unibo.scafi.simulation.gui.configuration.command
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArg, easyResultCreation}
import it.unibo.scafi.simulation.gui.util.Result

/**
  * a factory used to create exit command
  */
class ExitCommandFactory extends CommandFactory {
  override def name: String = "exit"

  override def description: String = "exit to application"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = easyResultCreation(() => System.exit(0))
}
