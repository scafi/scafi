package it.unibo.scafi.simulation

import it.unibo.scafi.simulation.SimulationObserver.{MovementEvent, SensorChangedEvent}
import it.unibo.utils.observer.{Event, Observer}

/**
  * an observer of scafi simulation
  * @tparam ID the id of node
  * @tparam NAME the name if local sensor
  */
class SimulationObserver[ID,NAME] extends Observer {
  private var idMovedInternal = collection.Set.empty[ID]
  import collection.mutable.{HashMap, MultiMap, Set}
  private val sensorChangedValue = new HashMap[ID,Set[NAME]] with MultiMap[ID,NAME]

  /**
    * @return the id moved in scafi simulation
    */
  def idMoved : collection.Set[ID] = {
    val res = idMovedInternal
    idMovedInternal = idMovedInternal -- res
    res
  }

  /**
    * @return the node sensor changed in scafi simulation
    */
  def idSensorChanged : collection.Map[ID,Set[NAME]] = {
    val res = sensorChangedValue.toMap
    sensorChangedValue --= res.keySet
    res
  }

  override def update(event: Event): Unit = event match {
    case MovementEvent(id) => idMovedInternal += id.asInstanceOf[ID]
    case SensorChangedEvent(id,name) => sensorChangedValue.addBinding(id.asInstanceOf[ID],name.asInstanceOf[NAME])
  }
}

object SimulationObserver {
  /**
    * event produced when a node is move to another position
    * @param id the node moved
    */
  case class MovementEvent(id : Any) extends Event

  /**
    * an event produced when a node change the value of a sensor
    * @param id the node id
    * @param name the sensor name
    */
  case class SensorChangedEvent(id : Any, name : Any) extends Event
}
