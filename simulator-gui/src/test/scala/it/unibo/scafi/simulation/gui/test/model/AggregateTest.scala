package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodesDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent._
import it.unibo.scafi.simulation.gui.model.space.{Point, Point3D}
import it.unibo.scafi.simulation.gui.test.help.AggregateWorldImpl
import org.scalatest.{FunSpec, Matchers}

class AggregateTest extends FunSpec with Matchers {
  val checkThat = new ItWord
  val world = new AggregateWorldImpl
  val zero = Point.ZERO
  val nodeNumber = 100
  val devProducer = new world.DeviceBuilder(world.led) :: new world.DeviceBuilder(world.motor) :: Nil
  val simpleNodeBuilder = new world.NodeBuilder(id = 0, position = zero)
  checkThat("i can add node in the world") {
    assert(world.insertNode(simpleNodeBuilder))
    assert(world.nodes.nonEmpty)
  }

  checkThat("I can remove node in the world") {
    world.clear()
    assert(world.insertNode(simpleNodeBuilder))
    assert(world.removeNode(0))
    assert(world.nodes.isEmpty)
  }

  checkThat("I can add multiple node in the world") {
    (0 until nodeNumber) foreach {x => world.insertNode(new world.NodeBuilder(id = x, position = zero))}
    assert(world.nodes.size == nodeNumber)
  }

  checkThat("i can take device of a node") {
    val nodeBuilder = new world.NodeBuilder(id = 0, position = zero, producer = devProducer)
    world.clear()
    world.insertNode(nodeBuilder)
    val node = world(0).get
    val dev = node.getDevice(world.led.name)
    assert(dev.isDefined)
    val fakeDev = node.getDevice("fake")
    assert(fakeDev.isEmpty)
    assert(node.devices.size == devProducer.size)
  }

  checkThat("i can move node in an aggregate world") {
    world.clear()
    world.insertNode(simpleNodeBuilder)
    val newPosition = Point3D(10,0,0)
    assert(world.moveNode(0,newPosition))
    val node = world(0).get
    assert(node.position == newPosition)
  }

  checkThat("i can observe all event") {
    val movedObserver = world.createObserver(Set(NodesMoved))
    val deviceChanged = world.createObserver(Set(NodesDeviceChanged))
    val insertNode = world.createObserver(Set(NodesAdded))
    val removedNode = world.createObserver(Set(NodesRemoved))
    assert(movedObserver.nodeChanged().isEmpty)
    assert(deviceChanged.nodeChanged().isEmpty)
    assert(removedNode.nodeChanged().isEmpty)
    assert(insertNode.nodeChanged().isEmpty)
    world <-- movedObserver <-- deviceChanged <-- insertNode <-- removedNode
    world.clear()

    world.insertNode(simpleNodeBuilder)
    assert(insertNode.nodeChanged().nonEmpty)
    assert(insertNode.nodeChanged().isEmpty)

    world.moveNode(0,Point3D(1,0,0))
    assert(movedObserver.nodeChanged().nonEmpty)
    assert(movedObserver.nodeChanged().isEmpty)

    world.addDevice(0,devProducer(0))
    assert(deviceChanged.nodeChanged().nonEmpty)
    assert(deviceChanged.nodeChanged().isEmpty)
    world.removeNode(0)
    assert(removedNode.nodeChanged().nonEmpty)
    assert(removedNode.nodeChanged().isEmpty)
  }

}
