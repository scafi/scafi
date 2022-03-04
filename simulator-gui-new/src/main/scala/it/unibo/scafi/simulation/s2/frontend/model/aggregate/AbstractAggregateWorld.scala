package it.unibo.scafi.simulation.s2.frontend.model.aggregate

import it.unibo.scafi.simulation.s2.frontend.model.aggregate.AggregateEvent._
import it.unibo.scafi.simulation.s2.frontend.model.common.world.AbstractObservableWorld
import it.unibo.scafi.simulation.s2.frontend.model.common.world.CommonWorldEvent
import it.unibo.scafi.simulation.s2.frontend.model.common.world.CommonWorldEvent.EventType

/**
 * a skeleton of aggregateWorld implementation
 */
trait AbstractAggregateWorld extends AggregateWorld with AbstractObservableWorld {
  self: AbstractAggregateWorld.Dependency =>

  override type O = AggregateWorldObserver
  override type MUTABLE_NODE <: AggregateMutableNode

  def moveNode(id: ID, position: P): Boolean = {
    val node = getNodeOrThrows(id)
    // move the node in the new position
    node.position = position
    this.notify(NodeEvent(node.id, NodesMoved))
    true
  }

  def addDevice(id: ID, deviceProducer: DEVICE_PRODUCER): Boolean = {
    val device = deviceProducer.build
    val node = getNodeOrThrows(id)
    // try to add new device in the node selected
    val added = node.addDevice(device)
    if (added) {
      // tell to all observer the world changes
      notify(NodeEvent(node.id, NodeDeviceChanged))
      notify(DeviceEvent(node.id, device.name, NodeDeviceAdded))
      true
    } else {
      false
    }
  }

  def removeDevice(id: ID, name: NAME): Boolean = {
    val node = getNodeOrThrows(id)
    // try to remove the device with the name selected
    val removed = node.removeDevice(name)
    if (removed) {
      // tell to all observer the world changes
      notify(NodeEvent(node.id, NodeDeviceChanged))
      notify(DeviceEvent(node.id, name, NodeDeviceRemoved))
      true
    } else {
      false
    }
  }

  /**
   * create a world observer
   * @param listenEvent
   *   the event that observer observe
   * @return
   *   the observer created
   */
  def createObserver(listenEvent: Set[EventType]): O = {
    val res = new AggregateWorldObserver(listenEvent)
    self.attach(res)
    res
  }
}

object AbstractAggregateWorld {
  type Dependency = AggregateWorld.Dependency
}
