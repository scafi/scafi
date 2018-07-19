package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi._
import it.unibo.scafi.simulation.gui.model.simulation.BasicSensors._
import it.unibo.scafi.simulation.gui.model.space.Point3D
import org.scalatest.{FunSpec, Matchers}

class BasicTestIncarnation extends FunSpec with Matchers{
  val checkThat = new ItWord
  val world  = SimpleScafiWorld
  val fakeRandomNumber = 10
  val fakeRadius = 10

  val bigNumber = 1000
  val proto = new world.ExternalNodePrototype(None)
  (0 to 1000) foreach {world + world.nodeFactory.create(_,Point3D(math.random,math.random,math.random),Set(),proto)}

  val aNode = 1
  val devName = "text"
  val devValue = "Hello"
  val anotherValue = "Bye"
  import ScafiLikeWorld._
  //TODO PRODUCE MORE TEST ON SENSOR
  val devProto = new world.ExternalDevicePrototype[Any](devValue,in)
  val dev = world.deviceFactory.create(devName,devProto)
  world.addDevices(world.nodes.map {x => x.id -> dev} toMap)
  checkThat("I can add device in a world") {
    val nodeDevice = world(aNode).get.getDevice(devName)
    assert(nodeDevice.isDefined)
    nodeDevice.get match {
      case OnOffSensor(_) => fail()
      case DisplaySensor(t) => assert(t == devValue)
      case _ => fail()
    }
  }

  checkThat("I can change a value in a device") {
    assert(world.changeSensorValue(aNode,devName,anotherValue))
    val nodeDevice = world(aNode).get.getDevice(devName)
    assert(nodeDevice.isDefined)
    nodeDevice.get match {
      case OnOffSensor(_) => fail()
      case DisplaySensor(t) => assert(t == anotherValue)
      case _ => fail()
    }
  }
}
