package it.unibo.scafi.simulation.gui.configuration.parser
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.util.Result

class AnyParser extends Parser [(CommandFactory,Map[String,Any])]{
  override def parse(arg: (CommandFactory,Map[String,Any])): (Result, Option[Command]) = arg._1.create(arg._2)
}
