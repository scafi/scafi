package it.unibo.scafi.simulation.s2.frontend.model.sensor

import it.unibo.scafi.simulation.s2.frontend.model.common.world.CommonWorldEvent.EventType

object SensorEvent {

  /**
   * an event produced when a sensor value change
   */
  object SensorChanged extends EventType
}
