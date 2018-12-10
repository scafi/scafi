package it.unibo.scafi.simulation.gui.configuration.parser

import it.unibo.scafi.simulation.gui.configuration.command.Command
import it.unibo.scafi.simulation.gui.util.Result

/**
  * parser is used to parse some argument into a command
  */
trait Parser[A]{
  /**
    * method used to parse argument into a command
    * @param arg the argument
    * @return (Fail,None) if the argument is not supported (Success,Some(command) otherwise
    */
  def parse(arg : A) : (Result,Option[Command])
}
