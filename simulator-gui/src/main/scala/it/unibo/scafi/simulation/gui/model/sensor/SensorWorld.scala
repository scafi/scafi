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
    node.getMutableDevice(name) match {
      case None => false
      case Some(device) =>
        device.value = value
        notify(NodeEvent(id, NodeDeviceChanged))
        notify(DeviceEvent(id, name, SensorChanged))
        true
    }
  }
}

object SensorWorld {
  type Dependency = AggregateWorld.Dependency
}
