package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.common.sensor.Sensor
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D}
import it.unibo.scafi.simulation.gui.test.help._
import org.scalatest.{FunSpec, Matchers}

class BasicSimulationTest extends FunSpec with Matchers{
  val checkThat = new ItWord
  val point = Point2D(1,1)
  val aggregateWorld = new BasicTestableAggregateWorld
  val dev = new BasicTestableAggregateDevice("mydevice",false)
  val superDevice = new BasicTestableAggregateDevice("adevice",true) with Sensor {
    override type VALUE = String

    override def value: VALUE = "nothing"
  }

  val node = new BasicTestableAggregateNode(id = 1,devices = Set(dev),position = Point.ZERO)
  val anotherNode = new BasicTestableAggregateNode(id = 2, devices = Set(dev), position = point)
  val lastNode = new BasicTestableAggregateNode(id = 3, devices = Set(dev), position = point)
  val lastId = 3
  val network = new TestSimulationPlatform

  network + node + anotherNode

  checkThat("network at begging must be empty") {
    network.neighbours().isEmpty
  }

  checkThat("I can't add a node that isn't in the network") {
    try {
      network.addNeighbours(node,Set(lastNode))
    } catch {
      case x : IllegalArgumentException => assert(true)
      case _ => assert(false)
    }
  }
  network + lastNode

  checkThat("I can add a neighbour ") {
    try {
      network.addNeighbours(node,Set(lastNode,anotherNode))
    } catch {
      case x : IllegalArgumentException => assert(false)
      case _ => assert(false)
    }
  }

  checkThat("network has a neighbour") {
    assert(network.neighbours(node).size == 2)
    assert(network.neighbours(node).find(_ == lastNode).isDefined)
  }
  checkThat("I can remove a neighbour") {
    assert(network.removeNeighbours(node,Set(lastNode)))
  }
  val bigNumber = 100000
  val maxTime = 0.5f

  network ++ (lastId to bigNumber).map(x => new BasicTestableAggregateNode(id = x,devices = Set(dev),position = Point.ZERO)).toSet
  checkThat("add or remove neighbour in big network doesn't take a lot of time") {
    Utils.timeTest(maxTime) {
      network.clearNeighbours(node)
      network.addNeighbours(node,network.nodes)
      network.removeNeighbours(node,network.nodes)
    }
  }
  checkThat("remove a node in the network doesn't take a lot of time") {
    network.addNeighbours(node,network.nodes)
    Utils.timeTest(maxTime) {
      assert(!network.neighbours(node).isEmpty)
      network.nodes.foreach(network.addNeighbours(_,Set(node)))
      network - node.id
      assert(network.neighbours(node).isEmpty)
    }
  }
}
