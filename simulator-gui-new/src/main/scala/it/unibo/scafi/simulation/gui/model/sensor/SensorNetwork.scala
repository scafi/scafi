package it.unibo.scafi.simulation.gui.model.sensor

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * describe a network of sensor attach on a node,
  * you can produce actuation on the world by changed
  * sensor value
  */
trait SensorNetwork extends SensorConcept {
  self : SensorNetwork.Dependency =>
  /**
    * change the value of sensor attach on a node
    * @param id the node id
    * @param name sensor name
    * @param value new value
    * @return true if the device is attached on node false otherwise
    * @throws IllegalArgumentException if the type sensor is not the same of parameter
    */
  def changeSensorValue[V](id : ID, name : NAME,value : V) : Boolean
}

object SensorNetwork {
  type Dependency = AggregateWorld with SensorConcept
}