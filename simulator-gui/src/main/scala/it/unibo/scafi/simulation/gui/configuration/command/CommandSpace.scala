package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.controller.input.Command.{CommandDescription, FailReason}

/**
  * describe e space when command is defined
  */
trait CommandSpace[DESCRIPTION <: CommandDescription] {
  /**
    * @return all command description of this command space
    */
  def descriptors: List[DESCRIPTION]

  /**
    * try to create a command from a string
    *
    * @param string the command in a string form
    * @return a command defined in this space
    */
  final def fromString(string: String): Option[Command] = {
    val commandList = descriptors.map { x => x.parseFromString(string)} filter {_.isDefined}
    if(commandList.isEmpty) None else commandList.head
  }

  /**
    * @return all command help (specified in this space)
    */
  final def helps : List[String] = descriptors map {_.help}

  /**
    * @return all command description (specified in this space)
    */
  final def descriptions : List[String] = descriptors map {_.description}
}

object CommandSpace {

  /**
    * the command fail because it called with illegal order
    */
  object illegalOrder extends FailReason {
    override def reason: String = "the command is called with illegal order"
  }
}

