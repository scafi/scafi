package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.model.core._
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

class BasicTestableObservableWorld extends ObservableWorld with SimpleSource {
  override type ID = Int
  override type NAME = String
  override type NODE = BasicTestableNode
  override type DEVICE = BasicTestableDevice
  override type P = Point2D
  override type S = Shape
  override type M = SimpleMetric
  class SimpleMetric extends Metric {
    override def positionAllowed(p: P): Boolean = true
  }
  override val metric: M = new SimpleMetric
  override val boundary: Option[B] = None

  //NB! test are concetered on the word! there aren't checks on device!
  class BasicTestableNode(override val id:ID,
                          override val position: Point2D,
                          private var device : Map[String,BasicTestableDevice]
                         ) extends Node{
    override def shape: Option[S] = None

    override def devices: Set[DEVICE] = device.values.toSet

    override def getDevice(name: String): Option[BasicTestableDevice] = device get name
  }

  class BasicTestableDevice(override val name : String) extends Device {
    private var _enable = false

    override def node = None
    override def state: Boolean = _enable
  }
}

