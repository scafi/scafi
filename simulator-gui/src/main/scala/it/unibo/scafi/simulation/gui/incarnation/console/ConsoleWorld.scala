package it.unibo.scafi.simulation.gui.incarnation.console
import it.unibo.scafi.simulation.gui.model.simulation.BasicPlatform
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

class ConsoleWorld extends BasicPlatform with SimpleSource {
  override type NODE = RootNode
  override type DEVICE = RootDevice
  override type NAME = String
  override type P = Point2D
  override type ID = Int
  override type NODE_FACTORY = RootNodeFactory
  override type DEVICE_FACTORY = RootDeviceFactory

  class RootDeviceFactory extends DeviceFactory{
    override def create(n: String, s: Boolean,node: Option[NODE]): RootDevice = new RootDevice(n,s,node)
  }
  class RootNodeFactory extends NodeFactory {
    override def create(id: Int, position: Point2D, shape: Option[S], devices: Set[RootDevice]): RootNode = new RootNode(id,position,devices)
  }

  class RootDevice(override val name : String, override val state : Boolean = false, override val node : Option[NODE] = None) extends Device {
    override def toString = s"RootDevice($name, $state)"
  }

  class RootNode(override val id : ID, override val position : Point2D, override val devices : Set[DEVICE] = Set[DEVICE]()) extends Node {
    require(devices forall {_.node.isEmpty})
    override def getDevice(name: NAME): Option[DEVICE] = this.devices.find(_.name == name)

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
  }

  override val nodeFactory: NODE_FACTORY = new NODE_FACTORY

  override val deviceFactory: DEVICE_FACTORY = new DEVICE_FACTORY

}