package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArg, easyResultCreation}
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.demo
import it.unibo.scafi.simulation.gui.util.Result

/**
  * a factory used to list all scafi demo
  */
class ListDemoCommandFactory extends CommandFactory {
  override val name: String = "list-demo"

  private lazy val scafiDemo = demo.demos.map {_.getSimpleName}

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = easyResultCreation(() => scafiDemo.foreach(println _))

}
