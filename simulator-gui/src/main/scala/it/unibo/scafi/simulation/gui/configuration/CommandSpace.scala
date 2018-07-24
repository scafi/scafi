package it.unibo.scafi.simulation.gui.configuration

import it.unibo.scafi.simulation.gui.controller.input.Command

/**
  * describe e space when command is defined
  */
trait CommandSpace {

  /**
    * try to create a command from a string
    * @param string the command in a string form
    * @return a command defined in this space
    */
  def fromString(string : String) : Option[Command]

}
