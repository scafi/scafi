package it.unibo.scafi.simulation.frontend.test.configuration

import it.unibo.scafi.simulation.frontend.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.frontend.configuration.parser.{ConfigurationMachine, Parser, VirtualMachine}
import it.unibo.scafi.simulation.frontend.util.Result
import it.unibo.scafi.simulation.frontend.util.Result.{Fail, Success}
import org.scalatest.{FunSpec, Matchers}

class ConfigurationMachineTest extends FunSpec with Matchers {
  private val checkThat = new ItWord
  private val command = "command"
  private val fakeCommand = "fake"
  private var called = 0
  private val parser = new Parser[String] {
    override def parse(arg: String): (Result, Option[Command]) = if(arg == command) {
    CommandFactory.creationSuccessful(onlyMakeCommand(() => {
      called += 1
      Success}))
    } else {
      CommandFactory.creationFailed(Fail("fail"))
    }
  }

  val configurationMachine = new ConfigurationMachine(parser)

  checkThat("i can process a command described in configuration machine parser") {
    val calledTimes = called
    val result = configurationMachine.process(command)
    result shouldEqual VirtualMachine.Ok
    assert(calledTimes != called)
  }

  checkThat("if i pass invalid arg machine result an error string") {
    val result = configurationMachine.process(fakeCommand)
    assert(result != VirtualMachine.Ok)
  }
}
