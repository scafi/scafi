package it.unibo.scafi.simulation.gui.configuration.command
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory._

/**
  * a factory used to list all command passed
  * @param commandFactories the command factories
  */
class ListCommandFactory(private val commandFactories: CommandFactory *) extends CommandFactory {
  override val name: String = "list-command"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = easyResultCreation(() => commandFactories.foreach(x => println(x.name)))
}
