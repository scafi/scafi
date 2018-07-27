package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.{AbstractAggregateWorld, AbstractNodeDefinition}
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld
import it.unibo.scafi.simulation.gui.model.sensor.{SensorConcept, SensorNetwork, SensorWorld}
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

/**
  * describe a platform skeleton
  */
object PlatformDefinition {

  /**
    * a generic platform
    */
  trait GenericPlatform extends AbstractAggregateWorld with SensorNetwork with ConnectedWorld with SimpleSource

  /**
    * a sensor platform :
    * describe a connected world with a sensor network
    */
  trait SensorPlatform extends SensorWorld
    with ConnectedWorld
    with SensorConcept
    with AbstractNodeDefinition
    with SimpleSource {
    override type SENSOR_VALUE = Any
  }

}
