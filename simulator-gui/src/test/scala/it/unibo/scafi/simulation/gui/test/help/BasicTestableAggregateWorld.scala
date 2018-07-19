package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.aggregate.implementation.immutable.AggregateWorld
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.space.Point3D
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
  override type NODE_PROTOTYPE = BasicNodePrototype
  override type DEVICE_PROTOTYPE = BasicDevicePrototype
  override type NODE = BasicTestableAggregateNode
  override type DEVICE = BasicTestableAggregateDevice

  override type NODE_FACTORY = BasicNodeFactory
  override type DEVICE_FACTORY = BasicDeviceFactory

  class BasicNodeFactory extends NodeFactory {
    override def create(id: Int,
                        position: Point3D,
                        devices: Set[BasicTestableAggregateDevice],
                        proto: NODE_PROTOTYPE): BasicTestableAggregateNode = new BasicTestableAggregateNode(id,devices,position,proto)
  }
  class BasicDeviceFactory extends DeviceFactory {
    override def create(n: String, proto: BasicDevicePrototype): BasicTestableAggregateDevice = new BasicTestableAggregateDevice(n)
  }
  override val boundary: Option[B] = None
  class BasicTestableAggregateNode(override val id : Int,
                                   override val devices : Set[BasicTestableAggregateDevice],
                                   override val position : Point3D,
                                   override val prototype: BasicNodePrototype = new BasicNodePrototype(None)) extends AggregateNode {

    def canEqual(other: Any): Boolean = other.isInstanceOf[BasicTestableAggregateNode]

    override def shape: Option[Shape2D] = prototype.shape

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

  class BasicNodePrototype(val shape: Option[Shape2D]) extends NodePrototype

  class BasicTestableAggregateDevice(val name : String) extends AggregateDevice {
    override def toString = s"BasicTestableAggregateDevice($name)"

    /**
      * @return the internal representation of node
      */
    override val prototype: DEVICE_PROTOTYPE = new BasicDevicePrototype
  }

  class BasicDevicePrototype extends DevicePrototype

  val nodeFactory: NODE_FACTORY = new BasicNodeFactory

  val deviceFactory: DEVICE_FACTORY = new BasicDeviceFactory

}
