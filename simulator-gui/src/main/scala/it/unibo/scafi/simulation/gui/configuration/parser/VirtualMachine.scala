package it.unibo.scafi.simulation.gui.configuration.parser

/**
  * an abstraction used to compute command, accepted an argument that is
  * try to parse by parser, if the parser create the command the virtual
  * machine execute
  * @tparam A
  */
trait VirtualMachine[A] {
  /**
    * the parser used by this machine
    * @return the parser
    */
  def parser() : Parser[A]

  /**
    * try to parse argument and execute the command created (if the parser parse the value)
    * @param argument the argument used to create command
    * @return a string that describe the command execution
    */
  def process(argument : A) : String
}

object VirtualMachine {
  val Ok = ""
}
