package it.unibo.scafi.simulation.gui.incarnation.console
import it.unibo.scafi.simulation.gui.model.aggregate.{AggregateDevice, AggregateNode}
import it.unibo.scafi.simulation.gui.model.common.network.TopologyDefinition.RandomTopology
import it.unibo.scafi.simulation.gui.model.common.world.MetricDefinition.CartesianMetric
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.model.core.Boundary
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.simulation.SimulationPlatform
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

class ConsoleWorld extends SimulationPlatform with SimpleSource {
  override type NODE = RootNode
  override type O = ObservableWorld.WorldObserver[NODE]
  override type B = Boundary[NODE#P,NODE#SHAPE]
  override type M = CartesianMetric[NODE#P]
  override val metric: M = new CartesianMetric[NODE#P]
  override val boundary: Option[B] = None
  override type T = RandomTopology[NODE]
  override val topology: T = new RandomTopology[NODE]
}

class RootDevice[N <: AggregateNode](override val name : String, override val state : Boolean = false, node : Option[N] = None) extends AggregateDevice {
  override type NAME = String
  override type NODE = N
  override protected def createDevice(state: Boolean, parent: Option[N]): this.type = new RootDevice(name,state,parent).asInstanceOf[this.type]

  override def toString = s"RootDevice($name, $state)"
}

class RootNode(override val id : Int, override val position : Point2D, devs : Set[AggregateDevice] = Set[AggregateDevice]()) extends AggregateNode {
  override type DEVICE = AggregateDevice
  require(devs forall {_.node.isEmpty})
  private var _devices : Set[DEVICE] = devs.asInstanceOf[Set[DEVICE]]
  override protected def copyNode(p: P, shape: Option[SHAPE], d: Set[DEVICE]): this.type = new RootNode(id,p,d).asInstanceOf[this.type]
  override type ID = Int
  override type P = Point2D
  override type SHAPE = Shape2D
  override def shape: Option[SHAPE] = None
  override def devices: Set[DEVICE] = this._devices
  override def getDevice(name: DEVICE#NAME): Option[DEVICE] = this._devices.find(_.name == name)

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
}
