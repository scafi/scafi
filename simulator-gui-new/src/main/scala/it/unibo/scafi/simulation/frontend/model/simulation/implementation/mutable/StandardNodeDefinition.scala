package it.unibo.scafi.simulation.frontend.model.simulation.implementation.mutable

import it.unibo.scafi.simulation.frontend.model.aggregate.AbstractNodeDefinition
import it.unibo.scafi.simulation.frontend.model.aggregate.AggregateEvent.NodeDeviceAdded
import it.unibo.scafi.simulation.frontend.model.sensor.SensorWorld
import it.unibo.utils.observer.Source

/**
  * standard implementation of node
  */
trait StandardNodeDefinition extends SensorDefinition with AbstractNodeDefinition {
  self: SensorWorld with Source =>
  override type NODE = Node

  override type MUTABLE_NODE = AbstractMutableNode

  override type NODE_PRODUCER = AbstractNodeBuilder

  /**
    * the implementation of node
    * @param id the node id
    * @param position the initial node position
    * @param shape the node shape
    */
  private class StandardNode(id: ID, position: P, shape: Option[S]) extends AbstractMutableNode(id, position, shape) {
    //performance reason
    private var deviceView : Set[DEVICE] = Set.empty
    override def view: NODE = this

    override def addDevice(device: MUTABLE_DEVICE): Boolean = {
      if(super.addDevice(device)){
        deviceView += device
        true
      } else {
        false
      }
    }

    override def removeDevice(name: NAME): Boolean = {
      val dev = this.devs.get(name)
      if(super.removeDevice(name)) {
        deviceView -= dev.get
        true
      } else {
        false
      }
    }

    override def devices: Set[DEVICE] = this.deviceView
  }

  /**
    * a single node builder, produce always the same node
    * @param id the node id
    * @param position the node position
    * @param shape the node shape
    * @param producer a list of device producer
    */
  class NodeBuilder(id : ID, position : P, shape : Option[S] = None, producer : List[DEVICE_PRODUCER] = List.empty)
    extends AbstractNodeBuilder(id,shape,position,producer) {

    override def build(): AbstractMutableNode = {
      val node = new StandardNode(id,position,shape)
      producer map {_.build} foreach {x =>
        node.addDevice(x)
        /*x.value[Any] match {
          case true =>  self.notify(DeviceEvent(node.id,x.name,NodeDeviceAdded))
          case "" => self.notify(DeviceEvent(node.id,x.name,NodeDeviceAdded))
          case _ => self.notify(DeviceEvent(node.id,x.name,NodeDeviceAdded))
        }*/
      }
      node
    }
  }
}
