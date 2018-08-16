package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Fail

/**
  * command pattern, a logic encapsulated inside a instance of command
  * a command can be undone
 */
sealed trait Command {
  /**
    * the logic of do in command
    * @return Success if there isn't error Fail otherwise
    */
  def make : () => Result

  /**
    * the logic of undo command
    * @return Success if there isn't error Fail otherwise
    *
    */
  def unmake : () => Result
}
object Command {

  /**
    * standard way to create a command
    * @param make the logic of do command
    * @param unmake the logic of undo
    */
  final case class command(override val make : () => Result) (override val unmake : () => Result) extends Command

  /**
    * a command that is reversable (the unmake has the same function)
    * @param make the logic of make and unmake
    */
  final case class reverseCommand(override val make : () => Result) extends Command {
    override val unmake: () => Result = make
  }

  /**
    * a command that isn't reverseble, return always fail in undo
    * @param make the logic of make
    */
  final case class onlyMakeCommand(override val make : () => Result) extends Command {
    override val unmake: () => Result = () => Fail("undo unsupported")
  }
}