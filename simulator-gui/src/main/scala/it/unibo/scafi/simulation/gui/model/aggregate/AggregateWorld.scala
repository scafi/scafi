package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld

/**
  * define a trait of aggregate world, you can make some actuation like
  * move node and attach/ detach device on node
  */
trait AggregateWorld extends ObservableWorld with AggregateConcept {
  self : AggregateWorld.Dependency =>

  /**
    * move a node in other position
    * @param id the node id
    * @param position the new position
    * @throws IllegalArgumentException if the node isn't in the world
    * @return true if the movement is allowed false otherwise
    */
  def moveNode(id : ID, position : P) : Boolean

  /**
    * add a device to a node in the world
    * @param id the node id
    * @param deviceProducer the device producer
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the node is in the world false otherwise
    */
  def addDevice(id: ID,deviceProducer : DEVICE_PRODUCER): Boolean

  /**
    * remove a device in a node in the world
    * @param id the node id
    * @param name the device name
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the node is in the world false otherwise
    */
  def removeDevice(id: ID,name : NAME): Boolean
}

object AggregateWorld {
  type Dependency = ObservableWorld.Dependency
}