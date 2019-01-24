package it.unibo.scafi.simulation.s2.frontend.model.simulation

import it.unibo.scafi.simulation.s2.frontend.model.aggregate.{AbstractAggregateWorld, AbstractNodeDefinition}
import it.unibo.scafi.simulation.s2.frontend.model.common.BoundaryDefinition
import it.unibo.scafi.simulation.s2.frontend.model.common.network.ConnectedWorld
import it.unibo.scafi.simulation.s2.frontend.model.sensor.{SensorConcept, SensorNetwork, SensorWorld}
import it.unibo.utils.observer.SimpleSource

/**
  * describe a platform skeleton
  */
object PlatformDefinition {

  /**
    * a generic sensor platform, node are connected with a network
    */
  trait GenericPlatform extends AbstractAggregateWorld with SensorNetwork with ConnectedWorld with SimpleSource

  /**
    * a sensor platform :
    * describe a connected world with a sensor network
    * in sensor platform you can:
    *   - change sensor value like an external actuation
    *   - move node inside world bounds (if it is defined)
    *   - add or remove device into node
    *   - add or remove node into world
    * for example you can with this code you can create ad hoc platform and insert node and device to change device status
    *
    * <pre>
    *   {@code
    *     //create ad hoc platform
    *     val platform = new SensorPlatform with World3D with StandardNodeDefinition with SensorDefinition with StandardNetwork {
    *       override type ID = String
    *        override type NAME = String
    *        override val boundary = None
    *     }
    *     platform.insertNode(new platform.NodeBuilder("node",Point3D(10,10,10)))
    *     val producers = List(platform.LedProducer("led",value = true,SensorConcept.sensorInput))
    *     platform.insertNode(new platform.NodeBuilder("root-node",Point3D(3,3,3),producer = producers))
    *     platform.moveNode("node",Point3D(0,0,0))
    *     platform.changeSensorValue("root-node","led",false)
    *     platform.removeDevice("node","led")
    * }
    * </pre>
    */
  trait SensorPlatform extends SensorWorld
    with ConnectedWorld
    with SensorConcept
    with AbstractNodeDefinition
    with SimpleSource
    with BoundaryDefinition {
  }
}
