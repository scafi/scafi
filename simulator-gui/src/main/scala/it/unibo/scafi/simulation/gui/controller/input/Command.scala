package it.unibo.scafi.simulation.gui.controller.input

import it.unibo.scafi.simulation.gui.controller.input.Command.CommandResult

import scala.util.Failure

/**
  * a command used to command pattern
  */
trait Command {
  /**
    * produced change
    */
  def make() : CommandResult

  /**
    * cancel the changes produced
    */
  def unmake() : CommandResult
}

object Command {

  /**
    * @param help help form used to get a description of how to create command via string
    * @param description the description of command
    */
  abstract class CommandDescription(val help : String, val description : String) {
    /**
      * try to create command descripted by this object
      * @param command
      */
    def parseFromString(command : String) : Option[Command]
  }

  /**
    * the result of command execution
     */
  sealed trait CommandResult

  /**
    * command success
    */
  final object Success extends CommandResult

  /**
    * command failure
    * @param reason reason of failure
    * @tparam E reason type
    */
  final case class Fail[E](reason : E) extends CommandResult

  /**
    * describe the reason of command fail
    */
  trait FailReason {
     def reason : String
  }

  /**
    * a command that can't be undo
    */
  trait WithoutUndoCommand extends Command {
    /**
      * @return always return fail
      */
    override final def unmake() = Fail("Can't undo")
  }
}