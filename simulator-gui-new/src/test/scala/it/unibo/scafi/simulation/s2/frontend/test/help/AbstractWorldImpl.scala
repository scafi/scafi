package it.unibo.scafi.simulation.s2.frontend.test.help

import it.unibo.scafi.simulation.s2.frontend.model.aggregate.{AbstractAggregateWorld, AbstractNodeDefinition}
import it.unibo.scafi.space.Shape
import it.unibo.utils.observer.SimpleSource

/**
  * a class used to test abstract aggregate world and aggregate node definition
  */
class AbstractWorldImpl extends StandardWorldDefinition with AbstractAggregateWorld with SimpleSource with AbstractNodeDefinition  {
  override type MUTABLE_NODE = AbstractMutableNode
  override type NODE_PRODUCER = AbstractNodeBuilder
  override protected type MUTABLE_DEVICE = RootMutableDevice
  override type DEVICE_PRODUCER = DeviceProducer
  override type DEVICE = Device

  //a device with only the name
  private class DeviceImpl(val name: String) extends RootMutableDevice {
    override def view: DEVICE = this
  }
  //a node implementation
  private class NodeImpl(id : ID, position : P, shape : Option[S]) extends AbstractMutableNode(id,position,shape) {
    override def view: NODE = this
  }

  // simple device producer definition
  trait DeviceType {
    def name : String
  }

  object Led extends DeviceType {
    val name = "Led"
  }
  object Position extends DeviceType {
    val name = "Position"
  }
  object Motor extends DeviceType {
    val name = "Motor"
  }

  class DeviceBuilder(deviceType : DeviceType) extends DeviceProducer {
    override def build: RootMutableDevice = new DeviceImpl(deviceType.name)
  }

  //simple node producer definition

  class NodeBuilder(id : ID, position : P, shape : Option[Shape] = None, producer : List[DEVICE_PRODUCER] = List.empty)
    extends AbstractNodeBuilder(id,shape,position,producer) {

    override def build(): AbstractMutableNode = {
      val node = new NodeImpl(id,position,shape)
      producer map {_.build} foreach {node.addDevice _}
      node
    }
  }

}
