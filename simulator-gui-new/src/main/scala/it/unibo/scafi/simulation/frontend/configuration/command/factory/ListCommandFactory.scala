package it.unibo.scafi.simulation.frontend.configuration.command.factory

import it.unibo.scafi.simulation.frontend.configuration.command.CommandFactory.{CommandArg, _}
import it.unibo.scafi.simulation.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.frontend.util.Result

/**
  * a factory used to list all command passed
  * @param commandFactories the command factories
  */
class ListCommandFactory(private val commandFactories: CommandFactory *) extends CommandFactory {
  override val name: String = "list-command"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = easyResultCreation(() => {
    import LogManager._
    val output = commandFactories.map {x => x.name} mkString "\n"
    LogManager.notify(StringLog(Channel.CommandResult,Label.Empty,output))
  })
}
