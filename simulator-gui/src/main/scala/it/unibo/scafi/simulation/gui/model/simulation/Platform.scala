package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.AbstractAggregateWorld
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld
/**
  * define a platform for simple simulation
  */
trait Platform extends AbstractAggregateWorld with ConnectedWorld {
  self : Platform.Dependency =>
  override type DEVICE <: AbstractSensor[Any]
  /**
    * the interface of a sensor
    * @tparam E the type of value
    */
  trait AbstractSensor[E] extends Device {
    /**
      * the value of server
      * @return the value
      */
    def value : E

    override def toString: String = super.toString + "value = " + value
  }
  /**
    * change the value of a sensor in the world
    * @param n the node with associated device
    * @param d the device name
    * @param value the new value
    * @tparam V the value of sensor
    * @throws IllegalArgumentException if the node isn't in the world of if the device isn't in the node
    * @return true if the value is different false otherwise
    */
  def changeSensorValue[V](n : ID, d : NAME, value: V) : Boolean

  /**
    * change a set of value associated to a sensor in the world
    * @param n the nodes
    * @param d the device name
    * @param value the new value
    * @tparam V the type of value
    * @throws IllegalArgumentException if the one node isn't in the world or if one device isn't associated to a node
    * @return empty set if all nodes are changed set of node not changed
    */
  def changeSensorValues[V](n : Set[ID], d : NAME , value : V) : Set[ID]
}

object Platform {
  type Dependency = AbstractAggregateWorld.Dependency
}


