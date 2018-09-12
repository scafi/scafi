package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.configuration.parser.{ConfigurationMachine, VirtualMachine}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiParser._
import org.scalatest.{FunSpec, Matchers}

class ConfigurationCommandTest extends FunSpec with Matchers {

  val checkThat = new ItWord

  val configurationMachine = new ConfigurationMachine(UnixConfiguration)

  checkThat("i can create a initializer with command describe in unix configuration") {
    val randomResult = configurationMachine.process("random-world 100 20 20")
    assert(randomResult == VirtualMachine.Ok)
    assert(scafiConfiguration.worldInitializer.isDefined)
    scafiConfiguration.worldInitializer = None
    val gridResult = configurationMachine.process("grid-world 20 10 10")
    assert(gridResult == VirtualMachine.Ok)
    assert(scafiConfiguration.worldInitializer.isDefined)
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
    val simulationResult = configurationMachine.process("radius-simulation Simple 20")
    assert(simulationResult == VirtualMachine.Ok)
    assert(scafiConfiguration.simulationInitializer.isDefined)
  }

  checkThat("i can't create a simulation initializer with wrong arg") {
    val simulationWrongDemoName = configurationMachine.process("radius-simulation class 10")
    assert(simulationWrongDemoName != VirtualMachine.Ok)
    val simulationWrongArgNumber = configurationMachine.process("radius-simulation Simple 10 10")
    assert(simulationWrongArgNumber != VirtualMachine.Ok)
  }

  checkThat("after set of command scafi configuration can built") {
    configurationMachine.process("random-world 1000 200 200")
    configurationMachine.process("radius-simulation Simple 10")
    assert(scafiConfiguration.create().isDefined)
  }
}

