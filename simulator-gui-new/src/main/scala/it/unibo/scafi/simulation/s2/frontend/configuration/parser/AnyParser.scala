package it.unibo.scafi.simulation.s2.frontend.configuration.parser
import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.util.Result

/**
 * a parser that try to create command with a map of string and any value
 */
class AnyParser extends Parser[(CommandFactory, Map[String, Any])] {
  override def parse(arg: (CommandFactory, Map[String, Any])): (Result, Option[Command]) = arg._1.create(arg._2)
}
