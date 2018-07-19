package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.implementation.immutable.AbstractObservableWorld

/**
  * this world define the main method to change node position and modify the set of node devices
  *
  */
//TODO CREATE AN OBSERVER THAT COULD BE OBSERVER DEVICE EVENT
trait AbstractAggregateWorld {
  self : AbstractAggregateWorld.Dependency =>
  /**
    * move a node in other position
    * @param n the node
    * @param p the new position
    * @throws IllegalArgumentException if the node isn't in the world
    * @return true if the movement is allowed false otherwise
    */
  def moveNode(n : ID, p : P) : Boolean

  /**
    * move a set of node in a new position
    * @param nodes the map of node and new position
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the node that can't be mode
    */
  def moveNodes(nodes : Map[ID,P]): Set[NODE]
  /**
    * add a device to a node in the world
    * @param n the node
    * @param d the device name
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the node is in the world false otherwise
    */
  def addDevice(n: ID,d : DEVICE): Boolean

  /**
    * insert device in a set of node
    * @param nodes the nodes and the device to add
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the set of node that can't add a device
    */
  def addDevices(nodes : Map[ID,DEVICE]) : Set[NODE]
  /**
    * remove a device in a node in the world
    * @param n the node
    * @param d the device name
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the node is in the world false otherwise
    */
  def removeDevice(n: ID,d : DEVICE): Boolean

  /**
    * remove a device in a set of node
    * @param nodes the nodes with the device associated
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the set of node that can't remove the device
    */
  def removeDevices(nodes : Map[ID,DEVICE]) : Set[NODE]
}
object AbstractAggregateWorld {
  type Dependency = AbstractObservableWorld with AbstractObservableWorld.Dependency
}