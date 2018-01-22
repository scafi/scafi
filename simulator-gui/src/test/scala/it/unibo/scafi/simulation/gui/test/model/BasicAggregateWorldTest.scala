package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodesDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.sensor.Sensor
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.{NodesAdded, NodesRemoved}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point3D}
import it.unibo.scafi.simulation.gui.test.help.BasicTestableAggregateWorld
import org.scalatest.{FunSpec, Matchers}

class BasicAggregateWorldTest extends FunSpec with Matchers{
  val checkThat = new ItWord
  val point = Point3D(1,1,2)
  val aggregateWorld = new BasicTestableAggregateWorld
  val dev = new aggregateWorld.BasicTestableAggregateDevice("mydevice",false)
  val superDevice = new aggregateWorld.BasicTestableAggregateDevice("adevice",true) with Sensor {
    override type VALUE = String

    override def value: VALUE = "nothing"
  }

  val node = new aggregateWorld.BasicTestableAggregateNode(id = 1,devices = Set(dev),position = Point.ZERO)
  val anotherNode = new aggregateWorld.BasicTestableAggregateNode(id = 2, devices = Set(dev), position = point)

  checkThat("clear queue of event") {
    val anObserver = aggregateWorld.createObserver(Set(NodesAdded,NodesRemoved,NodesMoved,NodesDeviceChanged))
    aggregateWorld <-- anObserver
    assert(anObserver.nodeChanged.isEmpty)
  }

  checkThat("i can add a node in the world") {
    assert(aggregateWorld.insertNode(node))
  }
  checkThat("i can change the position of a node in the world") {
    assert(aggregateWorld.moveNode(node.id,point))
    val changeNode= aggregateWorld(node.id)
    assert(changeNode.isDefined)
    assert(changeNode.get.position == point)
  }
  checkThat("i can change the state of device in a node in the aggregateWorld") {
    assert(aggregateWorld.switchOnDevice(node.id,dev.name))
    val changedNode = aggregateWorld(node.id)
    assert(changedNode.isDefined)
    val changedDev = changedNode.get.getDevice(dev.name)
    assert(changedDev.isDefined)
    assert(changedDev.get.state)
    assert(aggregateWorld.switchOffDevice(node.id,dev.name))
  }
  aggregateWorld + anotherNode
  checkThat("i can switch on a set of device") {
    assert(aggregateWorld.switchOnDevices( Map(node.id -> dev.name, anotherNode.id -> dev.name)).isEmpty)
  }
  checkThat("i can switch off a set of device") {
    assert(aggregateWorld.switchOffDevices( Map(node.id -> dev.name, anotherNode.id -> dev.name)).isEmpty)
  }
  checkThat("i can add a device") {
    assert(aggregateWorld.addDevice(node.id,superDevice))
    val changedNode = aggregateWorld(node.id)
    assert(changedNode.isDefined)
    assert(changedNode.get.getDevice(superDevice.name).isDefined)
    assert(aggregateWorld.removeDevice(node.id,superDevice))
    val anotherChangedNode = aggregateWorld(node.id)
    assert(anotherChangedNode.isDefined)
    assert(anotherChangedNode.get.getDevice(superDevice.name).isEmpty)
  }

  checkThat("i can add a set of device") {
    assert(aggregateWorld.addDevices(Map(node.id -> superDevice, anotherNode.id -> superDevice)).isEmpty)
  }

  checkThat("i can remove a set of device") {
    assert(aggregateWorld.removeDevices(Map(node.id -> superDevice, anotherNode.id -> superDevice)).isEmpty)
  }

  checkThat("multiple event store only the node changed") {
    val anObserver = aggregateWorld.createObserver(Set(NodesDeviceChanged))
    aggregateWorld <-- anObserver
    assert(anObserver.nodeChanged.isEmpty)
    aggregateWorld.switchOnDevice(node.id,dev.name)
    assert(anObserver.nodeChanged.size == 1)
    assert(anObserver.nodeChanged.isEmpty)
    aggregateWorld.switchOffDevice(node.id,dev.name)
    aggregateWorld.addDevice(node.id,superDevice)
    aggregateWorld.removeDevice(node.id,superDevice)
    assert(anObserver.nodeChanged().size == 1)
  }
}