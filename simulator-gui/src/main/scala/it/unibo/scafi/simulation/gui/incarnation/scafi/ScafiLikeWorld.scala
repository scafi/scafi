package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld.SensorType
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.simulation.BasicPlatform
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

/**
  * a world describe a plaftform like scafi
  */
trait ScafiLikeWorld extends BasicPlatform with SimpleSource{
  self : ScafiLikeWorld.Dependency =>
  override type ID = Int
  override type NAME = String
  override type P = Point3D
  override type DEVICE = BridgedSensor[Any]
  override type B = ShapeBoundary

  /**
    * a bridged sensor
    * @tparam E the type of value
    */
  trait BridgedSensor[E] extends Sensor[E] {
    def sensorType : SensorType
  }
}

object ScafiLikeWorld {
  trait SensorType

  object in extends SensorType
  object out extends SensorType
  type Dependency = AggregateWorld.Dependency
}

/**
  * an incarnation to a scafi like world
  */
object SimpleScafiWorld extends ScafiLikeWorld   {

  override type NODE = AggregateNode
  override type S = Shape2D
  override type NODE_PROTOTYPE = ExternalNodePrototype
  override type NODE_FACTORY = NodeFactory

  override type DEVICE_PROTOTYPE = ExternalDevicePrototype[Any]
  override type DEVICE_FACTORY = DeviceFactory
  override type M = Metric
  override val metric: Metric = new Metric {
    override def positionAllowed(p: Point3D): Boolean = true
  }
  var boundary: Option[B] = None
  class ExternalNodePrototype(override val shape: Option[S]) extends NodePrototype

  protected class InternalNode private[SimpleScafiWorld](val id : Int,
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

    override def toString = s"InternalNode($id, $position)"
  }

  class ExternalDevicePrototype[Any](val value : Any, val sensorType : SensorType) extends SensorPrototype[Any] {
    override def copy[V](value: V): DEVICE_PROTOTYPE = new ExternalDevicePrototype(value,sensorType)
  }

  class InternalSensor[E] private[SimpleScafiWorld](val name: String,
                                                         val value : E,
                                                    val sensorType : SensorType) extends BridgedSensor[E] {
    override val prototype: DEVICE_PROTOTYPE = new ExternalDevicePrototype(value,sensorType)

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

  class ExternalNodeFactory private[SimpleScafiWorld]() extends NodeFactory {
    override def create(id: Int,
                        position: Point3D,
                        devices: Set[BridgedSensor[Any]],
                        proto: ExternalNodePrototype): InternalNode = new InternalNode(id,proto.shape,position,devices)
  }
  class ExternalDeviceFactory private[SimpleScafiWorld]() extends DeviceFactory {
    override def create(n: String, proto: ExternalDevicePrototype[Any]): InternalSensor[Any] = new InternalSensor(n,proto.value,proto.sensorType)
  }
  val nodeFactory: NODE_FACTORY = new ExternalNodeFactory
  val deviceFactory: DEVICE_FACTORY= new ExternalDeviceFactory
}