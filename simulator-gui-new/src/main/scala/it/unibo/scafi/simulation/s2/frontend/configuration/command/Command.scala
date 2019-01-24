package it.unibo.scafi.simulation.s2.frontend.configuration.command

import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail

/**
  * command pattern, a logic encapsulated inside a instance of command
  * a command can be undone
 */
trait Command {
  /**
    * the execution command logic
    * @return Success if there isn't error Fail otherwise
    */
  def make : () => Result

  /**
    * the logic to remove changes of do method
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
    * a command that is reversible (the undo has the same function)
    * @param make the logic of make and undo
    */
  final case class reverseCommand(override val make : () => Result) extends Command {
    override val unmake: () => Result = make
  }

  /**
    * a command that isn't reversible, return always fail in undo
    * @param make the logic of do
    */
  final case class onlyMakeCommand(override val make : () => Result) extends Command {
    override val unmake: () => Result = () => Fail("undo unsupported")
  }
}