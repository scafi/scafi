package it.unibo.scafi.simulation.gui.model.sensor.implementation.mutable

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodesDeviceChanged
import it.unibo.scafi.simulation.gui.model.aggregate.implementation.mutable.{AbstractAggregateWorld, AggregateWorld}
import it.unibo.scafi.simulation.gui.model.sensor.SensorNetwork

trait SensorWorld extends AbstractAggregateWorld with SensorNetwork  {
  self: SensorWorld.Dependency =>

  override protected type MUTABLE_DEVICE = MutableSensor[SENSOR_VALUE]
  override type DEVICE = Sensor[SENSOR_VALUE]

  def changeSensorValue(id : ID, name : NAME,value : SENSOR_VALUE) : Boolean = {
    val node = getNodeOrThrows(id)
    //find the dev with name passed
    val dev = node.getMutableDevice(name)
    if(dev.isEmpty) {
      false
    } else {
      dev.get.value = value
      //notify the observer of world changes
      notify(WorldEvent(List(id),NodesDeviceChanged))
      true
    }
  }
}

object SensorWorld {
  type Dependency = AggregateWorld.Dependency
}
