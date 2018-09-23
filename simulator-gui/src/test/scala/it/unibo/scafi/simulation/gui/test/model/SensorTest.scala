package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodeDeviceChanged
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{sensorInput, sensorOutput}
import it.unibo.scafi.simulation.gui.test.help.SensorWorldImpl
import it.unibo.scafi.space.Point3D
import org.scalatest.{FunSpec, Matchers}

//noinspection NameBooleanParameters,NameBooleanParameters,NameBooleanParameters
class SensorTest extends FunSpec with Matchers {
  private val checkThat = new ItWord
  //sensor world with any type of sensor,
  private  val world = new SensorWorldImpl
  private val zero = Point3D.Zero
  private val nodeNumber = 100
  private val defaultName = "input"
  private val ledSensor = "led"
  private val generalSensor = "value"
  private val floatSensor = "temp"
  private val sensorBuilders : List[world.DEVICE_PRODUCER]= new world.LedProducer("led",false,sensorInput) ::
                       new world.GeneralSensorProducer("value",0,sensorInput) ::
                       new world.DoubleSensorValue("temp",10.5,sensorOutput) :: Nil
  checkThat("i can add node with sensor") {
    world.clear()
    val nodeBuilder = new world.NodeBuilder(0,zero)
    world.insertNode(nodeBuilder)
    assert(world.addDevice(0,world.LedProducer(defaultName,false,sensorInput)))
    val node = world(0).get
    assert(node.devices.nonEmpty)

    val dev = node.getDevice(defaultName)
    assert(dev.nonEmpty)

    val sensor = dev.get
    assert(!sensor.value[Boolean])

    //noinspection DangerousCatchAll
    try {
      world.changeSensorValue(0,defaultName,"aa")
      fail("i can't change sensor value to another sensor type")
    } catch {
      case e => assert(true)
    }
  }

  checkThat("i can't change sensor value of node that hasn't a sensor") {
    //noinspection DangerousCatchAll
    try {
      world.changeSensorValue(-1,defaultName,true)
      assert(false)
    } catch {
      case _ =>
    }
  }


  checkThat("i can create a sensor network") {
    world.clear()
    for (elem <- 0 until nodeNumber) {
      world.insertNode(new world.NodeBuilder(id = elem, position = zero, producer = sensorBuilders))
    }
    assert(world.nodes.nonEmpty)
    world.nodes.size shouldEqual nodeNumber

    assert(world.changeSensorValue(0,ledSensor,true))
    assert(world(0).get.getDevice(ledSensor).get.value != world(1).get.getDevice(ledSensor).get.value)
  }

  checkThat("observer can see the device changes") {
    world.clear()
    val observer = world.createObserver(Set(NodeDeviceChanged))
    world attach observer
    assert(observer.nodeChanged().isEmpty)
    world.insertNode(new world.NodeBuilder(id = 0, position = zero, producer = sensorBuilders))
    val newSensorValue = 1
    assert(world.changeSensorValue(0,floatSensor,newSensorValue))
    assert(observer.nodeChanged().contains(0))
  }
}
