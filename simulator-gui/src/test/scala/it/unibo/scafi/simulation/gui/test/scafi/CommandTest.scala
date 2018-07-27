package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.configuration.SensorName
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld.scafiWorldCommandSpace
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiWorldInitializer, scafiWorld}
import it.unibo.scafi.simulation.gui.model.space.Point3D
import org.scalatest.{FunSpec, Matchers}

class CommandTest extends FunSpec with Matchers {
  val checkThat = new ItWord

  val world = scafiWorld
  val node = 100
  val worldSize = 50
  val commandSpace = scafiWorldCommandSpace
  ScafiWorldInitializer.Random(node,worldSize,worldSize).init()

  checkThat("i can move node with move command") {
    val newPoint = Point3D(1,1,1)
    val map : Map[Any,Point3D] = Map(0 -> newPoint)
    val command = commandSpace.MoveCommand(map)
    val oldPos = world(0).get.position
    command.make()
    assert(world(0).get.position == newPoint)
    command.unmake()
    assert(world(0).get.position == oldPos)
  }

  checkThat("i can toggle a device") {
    val dev = world(0).get.getDevice(SensorName.sensor1).get
    val oldValue = dev.value
    val command = commandSpace.ToggleDeviceCommand(Set(0),SensorName)
    command.make()
    assert(dev.value != oldValue)
    command.unmake()
    assert(dev.value == oldValue)
  }
}
