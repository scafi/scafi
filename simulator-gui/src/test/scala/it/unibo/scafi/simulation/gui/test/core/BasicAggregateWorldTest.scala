package it.unibo.scafi.simulation.gui.test.core

import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D}
import it.unibo.scafi.simulation.gui.test.help.{BasicTestableAggregateDevice, BasicTestableAggregateNode, BasicTestableAggregateWorld}
import org.scalatest.{FunSpec, Matchers}

class BasicAggregateWorldTest extends FunSpec with Matchers{
  val aggregateWorld = new BasicTestableAggregateWorld
  val dev = new BasicTestableAggregateDevice("mydevice",false)
  val node = new BasicTestableAggregateNode(id = 1,devices = Set(dev),position = Point.ZERO)
  aggregateWorld + node
  println(aggregateWorld.moveNode(node,Point2D(1,3)))
  aggregateWorld.nodes.foreach(y => println(y.position.x))
}
