package it.unibo.scafi.simulation.frontend.configuration.parser

/**
  * an abstraction used to compute command, accepted an argument that is
  * try to parse by parser, if the parser create the command the virtual
  * machine execute
  * you can used virtual machine like this:
  * <pre>
  *   @{code
  *     val machine : VirtualMachine[String] = new ConfigurationMachine[String](...)
  *     machine.process("list-command")
  *   }
  * </pre>
  * the machine try to convert list-command in a command and after he execute it
  * @tparam A the type of input value that machine parse and execute
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
