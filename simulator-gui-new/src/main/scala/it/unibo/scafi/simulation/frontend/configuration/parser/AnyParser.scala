package it.unibo.scafi.simulation.frontend.configuration.parser
import it.unibo.scafi.simulation.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.frontend.util.Result

/**
  * a parser that try to create command with a map of string and any value
  */
class AnyParser extends Parser [(CommandFactory,Map[String,Any])]{
  override def parse(arg: (CommandFactory,Map[String,Any])): (Result, Option[Command]) = arg._1.create(arg._2)
}
