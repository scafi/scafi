package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.EventType

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
  object NodesDeviceChanged extends EventType
}
