package it.unibo.scafi.simulation.s2.frontend.configuration.command.factory

import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory.{CommandArg, easyResultCreation}
import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.util.Result

/**
  * a factory used to create exit command
  */
object ExitCommandFactory extends CommandFactory {
  override val name: String = "exit"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = easyResultCreation(() => System.exit(0))
}
