package it.unibo.scafi.simulation.gui.controller.input

/**
  * a command used to command pattern
  */
trait Command {
  /**
    * produced change
    */
  def make() : Unit

  /**
    * cancel the changes produced
    */
  def unmake() : Unit

  /**
    * try to create command by string passed
    * @param string the command write in string form
    * @return the command if the string is legit or none
    */
  def buildFromString(string : String) : Option[Command]
}
