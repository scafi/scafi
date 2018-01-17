package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.space.Point
import it.unibo.scafi.simulation.gui.test.help.Utils._
import it.unibo.scafi.simulation.gui.test.help._
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
  val simpleNode = new BasicTestableNode(simpleId,Point.ZERO,Map())
  //SECOND(WITH ONE DEVICE)
  val middleId = 2
  val middleNode = new BasicTestableNode(middleId,Point.ZERO,Map(deviceName -> simpleDevice))
  //THIRD(WITH ONE ADVANCE DEVICE)
  val lastId = 3
  val lastNode = new BasicTestableNode(lastId,Point.ZERO,Map(advanceName -> advanceDevice))

  checkThat("simple node hasn't device") {
    assert(simpleNode.getDevice(deviceName) isEmpty)
    assert((simpleNode devices) isEmpty)
  }

  checkThat("last node has a device") {
    assert(lastNode.getDevice(advanceName) isDefined)
    assert(!((lastNode devices) isEmpty))
  }

  val world = new BasicTestableObservableWorld {
    override type NODE = BasicTestableNode
  }
  val worldObserver = new BasicTestableWorldObserver[BasicTestableNode]
  val anotherWorldObserver = new BasicTestableWorldObserver[BasicTestableNode]
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
    assert((world insertNodes Set(middleNode,lastNode)).isEmpty)
  }

  checkThat("i can't add a node twice") {
    assert(!(world insertNode simpleNode))
  }

  checkThat("i can't add a set of node twice") {
    assert(!(world insertNodes Set(simpleNode,lastNode)).isEmpty)
  }

  val bigNumber = 100000
  val toSec = Math.pow(10,3)
  val maxSec = 1
  checkThat("adding a big number of element doesn't take a lot of time") {
    timeTest(maxSec) {
      world ++ (lastId to bigNumber).map(x => new BasicTestableNode(x,Point.ZERO,Map(simpleDevice.name ->simpleDevice))).toSet
    }
  }

  checkThat("adding a big number of observer doesn't take a lot of time") {
    timeTest(maxSec){
      for (elem <- (0 to bigNumber).map(x => new BasicTestableWorldObserver[BasicTestableNode])) {
        world <-- elem
      }
    }
  }

  checkThat("multiple notification doesn't take lot a time") {
    timeTest(maxSec) {
      world - simpleNode.id
    }
  }
}
