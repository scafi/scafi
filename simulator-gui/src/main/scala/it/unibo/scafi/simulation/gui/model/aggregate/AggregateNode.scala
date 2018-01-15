package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.core.Node

/***
  * a description of aggregate node.
  * it is immutable.
  * there are some method used to
  * copy current node and alter some
  * proprieties
  */
trait AggregateNode extends Node {
  override type DEVICE <: AggregateDevice
  /**
    * create a new node in the position passed
    * @param p the new position
    * @return the new node created
    */
  def movedTo(p: P) : AggregateNode

  /**
    * switch on a device in the new node
    * @param d the device name
    * @return the new node created
    */
  def turnOnDevice(d: DEVICE#NAME) : AggregateNode

  /**
    * switch off a device in the new nodde
    * @param d the device name
    * @return the new node created
    */
  def turnOffDevice(d: DEVICE#NAME) : AggregateNode

  /**
    * add a device in a new node
    * @param d the device
    * @return the new device created
    */
  def addDevice(d : DEVICE) : AggregateNode

  /**
    * remove a device in a new node
    * @param d the device
    * @return the new device created
    */
  def removeDevice(d : DEVICE) : AggregateNode
}
