package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.common.world.MetricDefinition.CartesianMetric
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.model.core._
import it.unibo.scafi.simulation.gui.model.space.{Point2D}
import it.unibo.scafi.simulation.gui.pattern.observer.Event

class BasicTestableObservableWorld extends ObservableWorld{

  override type NODE = BasicTestableNode

  override type B = Boundary

  override type M = CartesianMetric.type

  override val metric: Metric = CartesianMetric

  override val boundary: Option[Boundary] = None
}
//NB! test are concetered on the word! there aren't checks on device!
class BasicTestableNode(override val id:Int,
                        override val position: Point2D,
                        private var device : Map[String,BasicTestableDevice]
                       ) extends Node{
  override type ID = Int

  override type P = Point2D

  override type SHAPE = Shape

  override type DEVICE = BasicTestableDevice

  override def shape: Option[SHAPE] = None

  override def devices: Set[DEVICE] = device.values.toSet

  override def getDevice(name: String): Option[BasicTestableDevice] = device get name
}

class BasicTestableDevice(override val name : String) extends Device {
  private var _enable = false
  override type NAME = String

  override type NODE = BasicTestableNode
  override def node = None
  override def state: Boolean = _enable
}

class BasicTestableObserverWorld extends ObservableWorld.ObserverWorld {
  private var events = Set[Event]()

  override def !!(event: Event): Unit = {
    events += event
  }

  def eventCount() = events.size

  def clearQueue() = events = Set()

}