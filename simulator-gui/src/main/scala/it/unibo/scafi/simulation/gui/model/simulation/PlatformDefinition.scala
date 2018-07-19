package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.implementation.mutable.{AbstractAggregateWorld, AggregateNodeDefinition}
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld
import it.unibo.scafi.simulation.gui.model.core.{Shape, World}
import it.unibo.scafi.simulation.gui.model.sensor.SensorNetwork
import it.unibo.scafi.simulation.gui.model.sensor.implementation.mutable.{SensorDefinition, SensorWorld}
import it.unibo.scafi.simulation.gui.model.space.{Point2D, Point3D}
import it.unibo.scafi.simulation.gui.pattern.observer.{PrioritySource, SimpleSource}

object PlatformDefinition {

  /**
    * a generic platform
    */
  trait GenericPlatform extends AbstractAggregateWorld with SensorNetwork with ConnectedWorld with SimpleSource

  /**
    * a sensor platform :
    * describe a connected world with a sensor network
    */
  trait SensorPlatform extends SensorWorld with ConnectedWorld with SensorDefinition with AggregateNodeDefinition with SimpleSource

  /**
    * describe a 3D world
    */
  trait World3D {
    self : World =>
    override type P = Point3D
    override type S = Shape
  }

  /**
    * describe a 2D world
    */
  trait World2D {
    self : World =>
    override type P = Point2D
    override type S = Shape
  }
}
