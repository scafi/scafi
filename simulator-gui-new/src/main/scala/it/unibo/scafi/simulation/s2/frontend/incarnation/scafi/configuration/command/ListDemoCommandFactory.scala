package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory.{CommandArg, easyResultCreation}
import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.s2.frontend.launcher.scafi.ListDemo
import it.unibo.scafi.simulation.s2.frontend.util.Result

/**
  * a factory used to list all scafi demo
  */
class ListDemoCommandFactory extends CommandFactory {
  override val name: String = "list-demo"

  private lazy val scafiDemo = ListDemo.demos.map {_.getSimpleName}

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = easyResultCreation(() => {
    import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager._

    LogManager.notify(StringLog(Channel.CommandResult,Label.Empty,scafiDemo.mkString("\n")))
  })

}
