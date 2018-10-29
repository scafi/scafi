package it.unibo.scafi.simulation.frontend.test.model

import it.unibo.scafi.simulation.frontend.model.aggregate.AggregateEvent.{NodeDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.frontend.model.common.world.CommonWorldEvent._
import it.unibo.scafi.simulation.frontend.test.help.AbstractWorldImpl
import it.unibo.scafi.space.Point3D
import org.scalatest.{FunSpec, Matchers}

//noinspection NameBooleanParameters
class AggregateTest extends FunSpec with Matchers {
  private val checkThat = new ItWord
  private val world = new AbstractWorldImpl
  private val zero = Point3D.Zero
  private val nodeNumber = 100
  private val devProducer = new world.DeviceBuilder(world.led) :: new world.DeviceBuilder(world.motor) :: Nil
  private val simpleNodeBuilder = new world.NodeBuilder(id = 0, position = zero)
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
    world.nodes.size shouldBe nodeNumber
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
    node.devices.size shouldEqual devProducer.size
  }

  checkThat("i can remove device of a node") {
    world.clear()
    val nodeBuilder = new world.NodeBuilder(id = 0, position = zero, producer = devProducer)
    world.insertNode(nodeBuilder)
    assert(world.removeDevice(0, world.led.name))
    val node = world(0)
    assert(node.get.getDevice(world.led.name).isEmpty)
  }

  checkThat("i can move node in an aggregate world") {
    world.clear()
    world.insertNode(simpleNodeBuilder)
    val newPosition = Point3D(10,0,0)
    assert(world.moveNode(0,newPosition))
    val node = world(0).get
    node.position shouldEqual newPosition
  }

  checkThat("i can't move node that isn't in the world") {
    //noinspection DangerousCatchAll
    try {
      world.moveNode(-1,Point3D(0,0,0))
      assert(false)
    } catch {
      case _ =>
    }
  }
  checkThat("i can observe all event") {
    val movedObserver = world.createObserver(Set(NodesMoved))
    val deviceChanged = world.createObserver(Set(NodeDeviceChanged))
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

    world.addDevice(0,devProducer.head)
    assert(deviceChanged.nodeChanged().nonEmpty)
    assert(deviceChanged.nodeChanged().isEmpty)
    world.removeNode(0)
    assert(removedNode.nodeChanged().nonEmpty)
    assert(removedNode.nodeChanged().isEmpty)
  }

}
