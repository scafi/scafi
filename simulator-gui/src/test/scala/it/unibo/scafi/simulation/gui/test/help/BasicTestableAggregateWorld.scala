package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.aggregate.{AggregateDevice, AggregateNode, AggregateWorld}
import it.unibo.scafi.simulation.gui.model.common.world.MetricDefinition.CartesianMetric
import it.unibo.scafi.simulation.gui.model.core.Boundary
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource
class BasicTestableAggregateWorld extends AggregateWorld with SimpleSource{
  override type NODE = BasicTestableAggregateNode

  override type O = BasicTestableWorldObserver[NODE]
  /**
    * the type of boundary of the world
    */
  override type B = Boundary[NODE]

  override type M = CartesianMetric[NODE#P]

  override val metric: M = new CartesianMetric[NODE#P]

  override val boundary: Option[B] = None
}
class BasicTestableAggregateNode(override val id : Int,
                                 override val shape : Option[Shape2D] = None,
                                 override val devices : Set[BasicTestableAggregateDevice],
                                 override val position : Point2D) extends AggregateNode {
  override type DEVICE = BasicTestableAggregateDevice

  override protected def copyNode(position: P,
                                  shape: Option[SHAPE],
                                  devices: Set[DEVICE]): this.type = new BasicTestableAggregateNode(id,shape,devices,position).asInstanceOf[this.type]

  override type ID = Int

  override type P = Point2D

  override type SHAPE = Shape2D

  override def getDevice(name: DEVICE#NAME): Option[DEVICE] = this.devices.find(_.name == name)

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

class BasicTestableAggregateDevice(val name : String, val state: Boolean) extends AggregateDevice {
  override protected def createDevice(state: Boolean): this.type = new BasicTestableAggregateDevice(name,state).asInstanceOf[this.type]

  override type NAME = String

  override type NODE = AggregateNode

  override def node: Option[NODE] = None

  override def toString = s"BasicTestableAggregateDevice($name, $state)"
}
