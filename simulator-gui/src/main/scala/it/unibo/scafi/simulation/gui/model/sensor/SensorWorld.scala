package it.unibo.scafi.simulation.gui.model.sensor

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodeDeviceChanged
import it.unibo.scafi.simulation.gui.model.aggregate.{AbstractAggregateWorld, AggregateWorld}
import it.unibo.scafi.simulation.gui.model.sensor.SensorEvent.SensorChanged

/**
  * describe a sensor world
  */
trait SensorWorld extends AbstractAggregateWorld with SensorNetwork  {
  self: SensorWorld.Dependency =>

  override protected type MUTABLE_DEVICE <: MutableSensor[SENSOR_VALUE]
  override type DEVICE <: Sensor[SENSOR_VALUE]

  def changeSensorValue(id : ID, name : NAME,value : SENSOR_VALUE) : Boolean = {
    val node = getNodeOrThrows(id)
    //find the dev with name passed
    val dev = node.getMutableDevice(name)
    if(dev.isEmpty) {
      false
    } else {
      dev.get.value = value
      //notify the observer of world changes
      notify(NodeEvent(id,NodeDeviceChanged))
      notify(DeviceEvent(id,name,SensorChanged))
      true
    }
  }
}

object SensorWorld {
  type Dependency = AggregateWorld.Dependency
}
