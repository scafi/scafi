package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.configuration.SensorName
import it.unibo.scafi.simulation.gui.configuration.command.MoveCommandFactory.MoveArg
import it.unibo.scafi.simulation.gui.configuration.command.ToggleCommandFactory.ToggleArg
import it.unibo.scafi.simulation.gui.configuration.command.{MoveCommandFactory, ToggleCommandFactory}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiWorldInitializer, scafiWorld}
import it.unibo.scafi.simulation.gui.model.space.Point3D
import org.scalatest.{FunSpec, Matchers}

class CommandTest extends FunSpec with Matchers {
  val checkThat = new ItWord

  val world = scafiWorld
  val node = 100
  val worldSize = 50
  val toggleFactory = new ToggleCommandFactory(scafiWorld)
  val moveCommandFactory = new MoveCommandFactory(scafiWorld)
  ScafiWorldInitializer.Random(node,worldSize,worldSize).init(ScafiSeed.standard)

  checkThat("i can move node with move command") {
    val newPoint = Point3D(1,1,1)
    val map : Map[Any,Point3D] = Map(0 -> newPoint)
    val command = moveCommandFactory.create(MoveArg(map))
    assert(command.isDefined)
    val oldPos = world(0).get.position
    command.get.make()
    assert(world(0).get.position == newPoint)
    command.get.unmake()
    assert(world(0).get.position == oldPos)
  }

  checkThat("with wrong id type command fail") {
    val newPoint = Point3D(1,1,1)
    val map : Map[Any,Point3D] = Map("__" -> newPoint)
    val command = moveCommandFactory.create(MoveArg(map))
    assert(command.isEmpty)
  }
  /*
  checkThat("command parser work in world space") {
    val node = 1
    val deviceName = SensorName.sensor1
    val moveCommand = scafiWorldCommandSpace.fromString(s"move $node->10,10,0")
    assert(moveCommand.isDefined)
    moveCommand.get.make() match {
      case Success => assert(true)
      case Fail(_) => assert(false)
    }
    val toggleCommand = scafiWorldCommandSpace.fromString(s"toggle $node,$deviceName")
    assert(toggleCommand.isDefined)
    toggleCommand.get.make() match {
      case Success => assert(true)
      case Fail(_) => assert(false)
    }
  }*/
  checkThat("i can toggle a device") {
    val dev = world(0).get.getDevice(SensorName.sensor1).get
    val oldValue = dev.value
    val command = toggleFactory.create(ToggleArg(SensorName.sensor1,Set(0)))
    assert(command.isDefined)
    command.get.make()
    assert(dev.value != oldValue)
    command.get.unmake()
    assert(dev.value == oldValue)
  }
}
