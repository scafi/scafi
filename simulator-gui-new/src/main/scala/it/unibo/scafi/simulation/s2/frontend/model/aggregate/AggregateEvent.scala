package it.unibo.scafi.simulation.s2.frontend.model.aggregate

import it.unibo.scafi.simulation.s2.frontend.model.common.world.CommonWorldEvent.EventType

/**
  * define the type of event in an aggregate world
  */
object AggregateEvent {

  /**
    * this type of event is produced when a node change its position
    */
  object NodesMoved extends EventType

  /**
    * this type of event is produced when a node change the internal representation of device
    */
  object NodeDeviceChanged extends EventType

  /**
    * this event type is produced when a device is added on a node
    */
  object NodeDeviceAdded extends EventType

  /**
    * this event type is produced when a device is removed on a node
    */
  object NodeDeviceRemoved extends EventType
}
