package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.common.device.GraphicsDevice
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.space.{Point2D, Point3D}
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource
class BasicTestableAggregateWorld extends AggregateWorld with SimpleSource{

  override type ID = Int
  override type NAME = String
  override type P = Point3D
  override type S = Shape2D
  override type B = Boundary
  override type M = SimpleMetric
  class SimpleMetric extends Metric {
    override def positionAllowed(p: Point3D): Boolean = true
  }
  override val metric: M = new SimpleMetric
  override type NODE = BasicTestableAggregateNode
  override type DEVICE = BasicTestableAggregateDevice with GraphicsDevice

  override type NODE_FACTORY = BasicNodeFactory
  override type DEVICE_FACTORY = BasicDeviceFactory
  class BasicNodeFactory extends NodeFactory {
    override def create(id: Int,
                        position: Point3D,
                        shape: Option[Shape2D],
                        devices: Set[DEVICE]): BasicTestableAggregateNode = new BasicTestableAggregateNode(id,shape,devices,position)
  }
  class BasicDeviceFactory extends DeviceFactory {
    override def create(n: String, s: Boolean, node: Option[BasicTestableAggregateNode]): BasicTestableAggregateDevice = new BasicTestableAggregateDevice(n,s)
  }
  override val boundary: Option[B] = None
  class BasicTestableAggregateNode(override val id : Int,
                                   override val shape : Option[Shape2D] = None,
                                   override val devices : Set[BasicTestableAggregateDevice],
                                   override val position : Point3D) extends Node {

    override def getDevice(name: NAME): Option[DEVICE] = this.devices.find(_.name == name)

    def canEqual(other: Any): Boolean = other.isInstanceOf[BasicTestableAggregateNode]

    override def equals(other: Any): Boolean = other match {
      case that: BasicTestableAggregateNode =>
        (that canEqual this) &&
          id == that.id
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(id)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }

    override def toString = s"BasicTestableAggregateNode($id, $shape, $devices, $position)"
  }

  class BasicTestableAggregateDevice(val name : String, val state: Boolean) extends Device with GraphicsDevice {
    override def node: Option[NODE] = None

    override def toString = s"BasicTestableAggregateDevice($name, $state)"
  }

  override def nodeFactory: NODE_FACTORY = new BasicNodeFactory

  override def deviceFactory: DEVICE_FACTORY = new BasicDeviceFactory

}
