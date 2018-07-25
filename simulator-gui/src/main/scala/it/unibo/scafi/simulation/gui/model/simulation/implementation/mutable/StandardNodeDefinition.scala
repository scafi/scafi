package it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable

import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform

/**
  * standard implementation of node
  */
trait StandardNodeDefinition extends SensorDefinition {
  self: SensorPlatform =>
  override type NODE = Node

  override type MUTABLE_NODE = AbstractMutableNode

  override type NODE_PRODUCER = AbstractNodeBuilder

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
      producer map {_.build} foreach {node.addDevice(_)}
      node
    }
  }
}
