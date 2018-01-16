package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.common.sensor.Sensor
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D}
import it.unibo.scafi.simulation.gui.test.help.{BasicTestableAggregateDevice, BasicTestableAggregateNode, BasicTestableAggregateWorld, BasicTestableObserverWorld}
import org.scalatest.{FunSpec, Matchers}
class NodeTest extends FunSpec with Matchers {
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
  println("EHI")
  val observer = new BasicTestableObserverWorld with ObservableWorld.ObserverWorld
  aggregateWorld <-- observer
  aggregateWorld + node
  aggregateWorld + anotherNode
  println(observer.clearChange)
}