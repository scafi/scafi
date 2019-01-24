package it.unibo.scafi.simulation.s2.frontend.model.sensor

import it.unibo.scafi.simulation.s2.frontend.model.aggregate.AggregateEvent.NodeDeviceChanged
import it.unibo.scafi.simulation.s2.frontend.model.aggregate.{AbstractAggregateWorld, AggregateWorld}
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorEvent.SensorChanged

/**
  * describe a sensor world
  */
trait SensorWorld extends AbstractAggregateWorld with SensorNetwork  {
  self: SensorWorld.Dependency =>

  override protected type MUTABLE_DEVICE <: MutableSensor
  override type DEVICE <: Sensor

  def changeSensorValue[V](id : ID, name : NAME,value : V) : Boolean = {
    val node = getNodeOrThrows(id)
    //find the dev with name passed
    node.getMutableDevice(name) match {
      case None => false
      case Some(device) =>
        if(value != device.value) {
          device.value = value
          notify(NodeEvent(id, NodeDeviceChanged))
          notify(DeviceEvent(id, name, SensorChanged))
          true
        } else {
          false
        }

    }
  }
}

object SensorWorld {
  type Dependency = AggregateWorld.Dependency
}
