package it.unibo.scafi.simulation.frontend.test.scafi

import it.unibo.scafi.simulation.frontend.configuration.parser.{ConfigurationMachine, VirtualMachine}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiInformation._
import it.unibo.scafi.simulation.frontend.launcher.scafi.ListDemo
import org.scalatest.{FunSpec, Matchers}

class ConfigurationCommandTest extends FunSpec with Matchers {
  ListDemo.packageName = "it.unibo.scafi.simulation.gui.test.help"
  val checkThat = new ItWord

  val configurationMachine = new ConfigurationMachine(UnixConfiguration)

  checkThat("i can create a initializer with command describe in unix configuration") {
    val randomResult = configurationMachine.process("random-world 100 20 20")
    assert(randomResult == VirtualMachine.Ok)
    assert(configurationBuilder.worldInitializer.isDefined)
    configurationBuilder.worldInitializer = None
    val gridResult = configurationMachine.process("grid-world 20 10 10")
    assert(gridResult == VirtualMachine.Ok)
    assert(configurationBuilder.worldInitializer.isDefined)
  }

  checkThat("i can't create a world initializer with wrong command") {
    val wrongRandomTypeArg = configurationMachine.process("random-world 10.1 10 10")
    assert(wrongRandomTypeArg != VirtualMachine.Ok)
    val wrongRandomNumberArg = configurationMachine.process("random-world 10 10 10 10")
    assert(wrongRandomNumberArg != VirtualMachine.Ok)
    val wrongGridTypeArg = configurationMachine.process("grid-world 19 20 1.2")
    assert(wrongGridTypeArg != VirtualMachine.Ok)
    val wrongGridNumberArg = configurationMachine.process("grid-world 10 10 10 20")
    assert(wrongGridNumberArg != VirtualMachine.Ok)
  }

  checkThat("i can create a simulation initializer described in unix configuration") {
    val simulationResult = configurationMachine.process("radius-simulation ScafiSimpleDemo 20")
    assert(simulationResult == VirtualMachine.Ok)
    assert(configurationBuilder.simulationInitializer.isDefined)
  }

  checkThat("i can't create a simulation initializer with wrong arg") {
    val simulationWrongDemoName = configurationMachine.process("radius-simulation class 10")
    assert(simulationWrongDemoName != VirtualMachine.Ok)
    val simulationWrongArgNumber = configurationMachine.process("radius-simulation ScafiSimpleDemo 10 10")
    assert(simulationWrongArgNumber != VirtualMachine.Ok)
  }

  checkThat("after set of command scafi configuration can built") {
    configurationMachine.process("random-world 1000 200 200")
    configurationMachine.process("radius-simulation ScafiSimpleDemo 10")
    assert(configurationBuilder.create().isDefined)
  }
}

