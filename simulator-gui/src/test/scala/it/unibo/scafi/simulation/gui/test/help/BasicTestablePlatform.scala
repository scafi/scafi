package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.simulation.BasicPlatform
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

class BasicTestablePlatform extends BasicPlatform with SimpleSource{
  override type ID = Int
  override type NODE = InternalNode
  override type P = Point3D
  override type S = Shape2D
  override type NODE_PROTOTYPE = ExternalNodePrototype
  override type NODE_FACTORY = NodeFactory

  override type NAME = String
  override type DEVICE = Sensor[Any]
  override type DEVICE_PROTOTYPE = ExternalDevicePrototype[Any]
  override type DEVICE_FACTORY = DeviceFactory

  override type B = ShapeBoundary
  override type M = Metric
  override val metric: Metric = new Metric {
    override def positionAllowed(p: Point3D): Boolean = true
  }
  override val boundary: Option[B] = None
  class ExternalNodePrototype(override val shape: Option[S]) extends NodePrototype

  class InternalNode private[BasicTestablePlatform](val id : Int,
                                                    val shape : Option[S],
                                                    val position : P,
                                                    val devices : Set[DEVICE]) extends AggregateNode {
    override val prototype: NODE_PROTOTYPE = new ExternalNodePrototype(shape)

    def canEqual(other: Any): Boolean = other.isInstanceOf[InternalNode]

    override def equals(other: Any): Boolean = other match {
      case that: InternalNode =>
        (that canEqual this) &&
          id == that.id
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(id)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }
  class ExternalDevicePrototype[Any](val value : Any) extends SensorPrototype[Any] {
    override def copy[V](value: V): DEVICE_PROTOTYPE = new ExternalDevicePrototype(value)
  }
  class InternalSensor[E] private[BasicTestablePlatform](val name: String,
                           val value : E)extends Sensor[E] {
    override val prototype: DEVICE_PROTOTYPE = new ExternalDevicePrototype(value)

    def canEqual(other: Any): Boolean = other.isInstanceOf[InternalSensor[Any]]

    override def equals(other: Any): Boolean = other match {
      case that: InternalSensor[Any] =>
        (that canEqual this) &&
          name == that.name
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(name)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }

  class ExternalNodeFactory private[BasicTestablePlatform]() extends NodeFactory {
    override def create(id: Int,
                        position: Point3D,
                        devices: Set[Sensor[Any]],
                        proto: ExternalNodePrototype): InternalNode = new InternalNode(id,proto.shape,position,devices)
  }
  class ExternalDeviceFactory private[BasicTestablePlatform]() extends DeviceFactory {
    override def create(n: String, proto: ExternalDevicePrototype[Any]): InternalSensor[Any] = new InternalSensor(n,proto.value)
  }
  val nodeFactory: NODE_FACTORY = new ExternalNodeFactory
  val deviceFactory: DEVICE_FACTORY= new ExternalDeviceFactory

}
