package it.unibo.scafi.simulation.gui.incarnation.console
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

class ConsoleWorld extends AggregateWorld with SimpleSource {
  override type NODE = RootNode
  override type DEVICE = RootDevice
  override type NAME = String
  override type P = Point2D
  override type ID = Int
  override type NODE_FACTORY = RootNodeFactory
  override type DEVICE_FACTORY = RootDeviceFactory
  override type DEVICE_PROTOTYPE = FakeDevicePrototype
  override type NODE_PROTOTYPE = FakeNodePrototype
  override type M = CartesianMetric.type

  override val metric = CartesianMetric

  override val boundary = None
  object CartesianMetric extends Metric {
    override def positionAllowed(p: P): Boolean = true
  }
  class RootDeviceFactory extends DeviceFactory{
    override def create(n: String,proto : DEVICE_PROTOTYPE): RootDevice = new RootDevice(n)
  }
  class RootNodeFactory extends NodeFactory {
    override def create(id: Int, position: Point2D, devices: Set[RootDevice], proto: FakeNodePrototype): RootNode = new RootNode(id,position,devices)
  }

  class RootDevice(override val name : String) extends AggregateDevice {
    override def toString = s"RootDevice($name)"

    /**
      * @return the internal representation of node
      */
    override def prototype: FakeDevicePrototype = new FakeDevicePrototype
  }

  class RootNode(override val id : ID, override val position : Point2D, override val devices : Set[DEVICE] = Set[DEVICE]()) extends AggregateNode {
    override def toString = s"RootNode($id, $position, $devices)"

    def canEqual(other: Any): Boolean = other.isInstanceOf[RootNode]

    override def equals(other: Any): Boolean = other match {
      case that: RootNode =>
        (that canEqual this) &&
          id == that.id
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(id)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }

    override def shape: Option[S] = None

    /**
      * @return the internal representation of node
      */
    /**
      * @return the internal representation of node
      */
    override def prototype: FakeNodePrototype = new FakeNodePrototype
  }
  class FakeNodePrototype extends NodePrototype {
    /**
      * @return a shape of a generic node
      */
    override def shape: Option[S] = None
  }

  class FakeDevicePrototype extends DevicePrototype
  override val nodeFactory: NODE_FACTORY = new NODE_FACTORY

  override val deviceFactory: DEVICE_FACTORY = new DEVICE_FACTORY

}