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
  override type DEVICE = InternalSensor[Any]
  override type DEVICE_PROTOTYPE = ExternalDevicePrototype[Any]
  override type DEVICE_FACTORY = DeviceFactory

  override type B = Boundary
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
  }
  class ExternalDevicePrototype[E](val value : E) extends SensorPrototype[E] {
    override def copy[V](value: V): DEVICE_PROTOTYPE = new ExternalDevicePrototype(value)
  }
  class InternalSensor[E] private[BasicTestablePlatform](val name: String,
                           val state: Boolean,
                           val value : E)extends Sensor[E] {
    override val prototype: DEVICE_PROTOTYPE = new ExternalDevicePrototype(value)
  }

  class ExternalNodeFactory private[BasicTestablePlatform]() extends NodeFactory {
    override def create(id: Int,
                        position: Point3D,
                        devices: Set[InternalSensor[Any]],
                        proto: ExternalNodePrototype): InternalNode = new InternalNode(id,proto.shape,position,devices)
  }
  class ExternalDeviceFactory private[BasicTestablePlatform]() extends DeviceFactory {
    override def create(n: String, s: Boolean, proto: ExternalDevicePrototype[Any]): InternalSensor[Any] = new InternalSensor(n,s,proto.value)
  }
  val nodeFactory: NODE_FACTORY = new ExternalNodeFactory
  val deviceFactory: DEVICE_FACTORY= new ExternalDeviceFactory

}
