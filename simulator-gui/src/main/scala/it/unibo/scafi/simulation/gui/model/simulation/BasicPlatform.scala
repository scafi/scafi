package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodesDeviceChanged
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld

import scala.collection.mutable
/**
  * define a platform for simple simulation
  */
trait BasicPlatform extends AggregateWorld with ConnectedWorld{
  self : BasicPlatform.Dependency =>
  override type DEVICE <: Sensor[Any]
  override type DEVICE_PROTOTYPE <: SensorPrototype[Any]
  override type NET = Network

  private val net : NET = new NetworkImpl

  override def network: NET = net
  /**
    * the interface of a sensor
    * @tparam E the type of value
    */
  trait Sensor[E] extends AggregateDevice {
    /**
      * the value of server
      * @return the value
      */
    def value : E

    override def toString: String = super.toString + "value = " + value
  }
  /**
    * the prototype of sensor
   */
  trait SensorPrototype[E] extends DevicePrototype {
    def value : E
    def copy[V](value: V) : DEVICE_PROTOTYPE
  }
  /**
    * change the value of a sensor in the world
    * @param n the node with associated device
    * @param d the device name
    * @param value the new value
    * @tparam V the value of sensor
    * @throws IllegalArgumentException if the node isn't in the world of if the device isn't in the node
    * @return true if the value is different false otherwise
    */
  def changeSensorValue[V](n : ID, d : NAME, value: V) : Boolean = {
    val (node,device) = this.getNodeAndDeviceOrThrows(n,d)
    if(device.value == value) return false
    changeNode(node,device,value)
    this.!!!(WorldEvent(Set(n),NodesDeviceChanged))
    true
  }

  /**
    * change a set of value associated to a sensor in the world
    * @param n the nodes
    * @param d the device name
    * @param value the new value
    * @tparam V the type of value
    * @throws IllegalArgumentException if the one node isn't in the world or if one device isn't associated to a node
    * @return empty set if all nodes are changed set of node not changed
    */
  def changeSensorValues[V](n : Set[ID], d : NAME , value : V) : Set[ID] = {
    val nodes = this.apply(n)
    require(nodes.size == n.size)
    require(nodes forall {_.getDevice(d).isDefined})
    val toChange = nodes filter {_.getDevice(d).get != value}
    toChange foreach (x => changeNode(x,x.getDevice(d).get,value))
    this.!!!(WorldEvent(toChange map {_.id}, NodesDeviceChanged))
    n -- (toChange map {_.id})
  }

  private def changeNode[V](n : NODE, d : DEVICE, v : V) = {
    val newDev = this.deviceFactory.copy(d)(proto = d.prototype.copy(v))
    val newNode = this.nodeFactory.copy(n)(devices = (n.devices - newDev) + newDev)
    this.removeBeforeEvent(Set(newNode.id))
    this.addBeforeEvent(Set(newNode))
  }
  private def getNodeAndDeviceOrThrows(n : ID, d : NAME) : (NODE,DEVICE)= {
    require(this.apply(n).isDefined)
    val node = this.apply(n).get
    require(node.getDevice(d).isDefined)
    (node,node.getDevice(d).get)
  }

  private class NetworkImpl extends Network {
    private var neigh : mutable.Map[ID,Set[ID]] = mutable.Map.empty
    /**
      * the neighbours of a node
      *
      * @param n the node
      * @return a set of neighbours
      */
    override def neighbours(n: ID): Set[ID] = neigh.getOrElse(n,Set())

    /**
      * the neighbour in the world
      *
      * @return the network
      */
    override def neighbours(): Map[ID, Set[ID]] = neigh toMap

    /**
      * set a neighbours of a node
      *
      * @param node      the node in thw world
      * @param neighbour the neighbour
      */
    override def setNeighbours(node: ID, neighbour: Set[ID]): Unit = neigh += node -> neighbour
  }

  class ShapeBoundary(val shape : S) extends Boundary {
    override def nodeAllowed(p: P, s: Option[S]): Boolean = shape.contains(p)
  }
}

object BasicPlatform {
  type Dependency = AggregateWorld.Dependency
}


