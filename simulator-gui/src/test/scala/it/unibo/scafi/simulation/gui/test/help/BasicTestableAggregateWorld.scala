package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.aggregate.{AggregateDevice, AggregateNode, AggregateWorld}
import it.unibo.scafi.simulation.gui.model.common.world.MetricDefinition.CartesianMetric
import it.unibo.scafi.simulation.gui.model.core.{Boundary, Metric}
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.space.Point2D
class BasicTestableAggregateWorld extends AggregateWorld {
  override  type NODE = BasicTestableAggregateNode
  /**
    * the type of boundary of the world
    */
  override type B = Boundary

  override type M = CartesianMetric.type

  override val metric: Metric = CartesianMetric

  override val boundary: Option[Boundary] = None
}
class BasicTestableAggregateNode(override val id : Int,
                                 override val shape : Option[Shape2D] = None,
                                 override val devices : Set[BasicTestableAggregateDevice],
                                 override val position : Point2D) extends AggregateNode {
  override type DEVICE = BasicTestableAggregateDevice

  override protected def copyNode(position: P,
                                  shape: Option[SHAPE],
                                  devices: Set[DEVICE]): this.type = new BasicTestableAggregateNode(id,shape,devices,position).asInstanceOf[this.type]

  /**
    * the type of id
    */
  override type ID = Int
  /**
    * a generic position of the node
    */
  override type P = Point2D
  /**
    * a shape that describe the node
    */
  override type SHAPE = Shape2D
  /**
    * the id associated with this node
    */
  /**
    *
    * @param name of device
    * @return a device if it is attach on the node
    */
  override def getDevice(name: DEVICE#NAME): Option[DEVICE] = this.devices.find(_.name == name)
}

class BasicTestableAggregateDevice(val name : String, val state: Boolean) extends AggregateDevice {
  override protected def createDevice(state: Boolean): BasicTestableAggregateDevice.this.type = ???

  /**
    * the name of device
    */
  override type NAME = String
  /**
    * the node where the device is attached
    */
  override type NODE = AggregateNode

  override def node: Option[NODE] = None
}
