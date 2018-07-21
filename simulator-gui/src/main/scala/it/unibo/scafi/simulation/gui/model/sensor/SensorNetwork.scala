package it.unibo.scafi.simulation.gui.model.sensor

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * describe a network of sensor attach on a device
  */
trait SensorNetwork extends SensorConcept {
  self : SensorNetwork.Dependency =>
  //the sensor value on this network
  type SENSOR_VALUE
  /**
    * change the value of sensor attach on a node
    * @param id the node id
    * @param name sensor name
    * @param value new value
    * @return true if the device is attached on node false otherwise
    * @throws IllegalArgumentException if the type sensor is not the same of parameter
    */
  def changeSensorValue(id : ID, name : NAME,value : SENSOR_VALUE) : Boolean
}

object SensorNetwork {
  type Dependency = AggregateWorld
}