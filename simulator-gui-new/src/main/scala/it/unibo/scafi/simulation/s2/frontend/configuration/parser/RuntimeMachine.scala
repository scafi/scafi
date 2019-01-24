package it.unibo.scafi.simulation.s2.frontend.configuration.parser

import it.unibo.scafi.simulation.s2.frontend.controller.input.InputCommandController
import it.unibo.scafi.simulation.s2.frontend.util.Result.{Fail, Success}

/**
  * runtime machine used to process command at runtime
  * @param parser the parser used by the runtime machine
  */
class RuntimeMachine[A](override val parser: Parser[A]) extends VirtualMachine[A] {
  import VirtualMachine._
  override def process(line : A) : String = {
    val res = parser.parse(line)
    res._1 match {
      case Fail(value) => value.toString
      case Success =>
        InputCommandController.exec(res._2.get)
        Ok
    }
  }
}
