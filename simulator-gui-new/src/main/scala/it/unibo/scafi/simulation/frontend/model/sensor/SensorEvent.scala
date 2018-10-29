package it.unibo.scafi.simulation.frontend.model.sensor

import it.unibo.scafi.simulation.frontend.model.common.world.CommonWorldEvent.EventType

object SensorEvent {

  /**
    * an event produced when a sensor value change
    */
  object SensorChanged extends EventType
}
