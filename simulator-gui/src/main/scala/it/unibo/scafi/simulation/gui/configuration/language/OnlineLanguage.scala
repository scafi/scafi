package it.unibo.scafi.simulation.gui.configuration.language

import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.controller.input.inputCommandController

/**
  * this language can be used at program runtime and produced change in the world, to do this, the commands
  * are executed in input controller
  * @param parsers the set of string parser and factory
  */
class OnlineLanguage (override protected val parsers : Map[Language.StringCommandParser,CommandFactory]) extends Language {
  override protected def computeCommand(command: Command): String = {
    inputCommandController.exec(command)
    Language.Ok
  }
}
