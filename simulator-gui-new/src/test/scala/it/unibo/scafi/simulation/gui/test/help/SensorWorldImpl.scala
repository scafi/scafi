package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.aggregate.AbstractNodeDefinition
import it.unibo.scafi.simulation.gui.model.sensor.SensorWorld
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.SensorDefinition
import it.unibo.scafi.space.Shape
import it.unibo.utils.observer.SimpleSource

class SensorWorldImpl extends StandardWorldDefinition
                      with AbstractNodeDefinition
                      with SensorDefinition
                      with SensorWorld
                      with SimpleSource {
  override type MUTABLE_NODE = AbstractMutableNode
  override type NODE_PRODUCER = AbstractNodeBuilder
  override protected type MUTABLE_DEVICE = MutableSensor
  override type DEVICE_PRODUCER = DeviceProducer
  override type DEVICE = Sensor
  //a node implementation
  private class NodeImpl(id : ID, position : P, shape : Option[S]) extends AbstractMutableNode(id,position,shape) {
    override def view: NODE = this
  }

  class NodeBuilder(id : ID, position : P, shape : Option[Shape] = None, producer : List[DEVICE_PRODUCER] = List.empty)
    extends AbstractNodeBuilder(id,shape,position,producer) {

    override def build(): MUTABLE_NODE = {
      val node = new NodeImpl(id,position,shape)
      producer map {_.build} foreach {node.addDevice(_)}
      node.asInstanceOf[MUTABLE_NODE]
    }
  }
}
