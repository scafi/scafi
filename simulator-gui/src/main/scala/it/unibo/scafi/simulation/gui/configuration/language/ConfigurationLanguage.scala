package it.unibo.scafi.simulation.gui.configuration.language
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * this language is used before the launch of program, the command is exec directly here
  * @param parsers the set of string parser and factory
  */
class ConfigurationLanguage(override protected val parsers : Map[Language.StringCommandParser,CommandFactory]) extends Language {
  override protected def computeCommand(command: Command): String = command.make() match {
    case Success => Language.Ok
    case Fail(e) => e.toString
  }
}
