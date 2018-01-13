package it.unibo.scafi.simulation.gui.model.common.node

import it.unibo.scafi.simulation.gui.model.core.Node

/**
  * a generic factory that produced NODE
 *
  * @tparam NODE the node that the factory produced
  */
trait NodeFactory[NODE <: Node] {
  /**
    * the prototype of a generic node
    */
  type PROTOTYPE <: NodePrototype

  /**
    * create a node associated with the prototype
    * @param prototype
    * @return the new node
    */
  def createNode(prototype: PROTOTYPE) : NODE

  /**
    * copy the node passed with new position
    * @param n the node
    * @param p the new position
    * @return the node copied
    */
  def copyNodePosition(n : NODE, p : NODE#P) : NODE

  /**
    * copy the node passed with new device
    * @param n the node
    * @param d the new device
    * @return the node copied
    */
  def addNodeDevice(n: NODE, d : NODE#DEVICE) : NODE

  /**
    * copy the node passed without a device
    * @param n the node
    * @param d the device to remove
    * @return the node copied
    */
  def removeNodeDevice(n: NODE, d : NODE#DEVICE) : NODE

  trait NodePrototype

}
