package it.unibo.scafi.simulation.gui.test.core

import it.unibo.scafi.simulation.gui.model.common.network.ObservableNetwork
import it.unibo.scafi.simulation.gui.model.common.network.TopologyDefinition.RandomTopology
import it.unibo.scafi.simulation.gui.model.space.Position
import it.unibo.scafi.simulation.gui.test.help.{BasicTestableDevice, BasicTestableNode, BasicTestableObservableWorld, BasicTestableObserverWorld}
import org.scalatest.{FunSpec, Matchers}
/*test the basic structure of observable world and observer pattern */
class BasicObservableWorldTest extends FunSpec with Matchers{
  val checkThat = new ItWord
  //DEFINITION OF SOME DEVICE
  //FIRST
  val deviceName = "simple"
  val simpleDevice = new BasicTestableDevice(deviceName)
  //SECOND
  val advanceName = "advance"
  val advanceDevice = new BasicTestableDevice(advanceName)
  //DEFINITION OF SOME NODE
  //FIRST(WITHOUT DEVICE)
  val simpleId = 1
  val simpleNode = new BasicTestableNode(simpleId,Position.ZERO,Map())
  //SECOND(WITH ONE DEVICE)
  val middleId = 2
  val middleNode = new BasicTestableNode(middleId,Position.ZERO,Map(deviceName -> simpleDevice))
  //THIRD(WITH ONE ADVANCE DEVICE)
  val lastId = 3
  val lastNode = new BasicTestableNode(lastId,Position.ZERO,Map(advanceName -> advanceDevice))

  checkThat("simple node hasn't device") {
    assert(simpleNode.getDevice(deviceName) isEmpty)
    assert((simpleNode devices) isEmpty)
  }

  checkThat("last node has a device") {
    assert(lastNode.getDevice(advanceName) isDefined)
    assert(!((lastNode devices) isEmpty))
  }

  val world = new BasicTestableObservableWorld
  val worldObserver = new BasicTestableObserverWorld
  val anotherWorldObserver = new BasicTestableObserverWorld
  world <-- worldObserver <-- anotherWorldObserver
  //test detach
  world <--! anotherWorldObserver
  checkThat("world doesn't has node at begging") {
    assert((world nodes) isEmpty)
    assert((world(simpleId) isEmpty))
    assert((world(middleId)) isEmpty)
    assert((world(lastId)) isEmpty)
  }

  checkThat("i can add a generic node") {
    assert(world insertNode simpleNode)
    assert(!((world nodes) isEmpty))
    assert((world(simpleId)) isDefined)
  }
  checkThat("observer is notify") {
    assert(worldObserver.eventCount() != 0)
    assert(anotherWorldObserver.eventCount() == 0)
  }
  checkThat("i can add a set of node") {
    assert(world insertNodes Set(middleNode,lastNode))
  }

  checkThat("i can't add a node twice") {
    assert(!(world insertNode simpleNode))
  }

  checkThat("i can't add a set of node twice") {
    assert(!(world insertNodes Set(simpleNode,lastNode)))
  }
  val network : ObservableNetwork = new BasicTestableObservableWorld with ObservableNetwork {
    override type T = RandomTopology.type
    override val topology: T = RandomTopology
  }

  checkThat("the network is empty") {
    assert((network neighbours) isEmpty)
  }
}
