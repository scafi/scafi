package it.unibo.scafi.simulation.gui.configuration.parser

import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * configuration machine used to process configuration command
  * @param parser the parser used by the runtime machine
  */
class ConfigurationMachine[A](override val parser: Parser[A]) extends VirtualMachine[A]{
  import VirtualMachine._
  override def process(line: A) : String = {
    val res = parser.parse(line)
    res._1 match {
      case Fail(value) => value.toString
      case Success =>
        res._2.get.make() match {
          case Fail(value) => value.toString
          case _ => Ok
      }
    }
  }
}
