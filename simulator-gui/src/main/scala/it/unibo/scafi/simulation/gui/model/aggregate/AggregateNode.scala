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
  //FACTORY METHOD
  protected def copyNode(position : P = this.position, shape : Option[SHAPE] = this.shape, devices : Set[DEVICE] = this.devices) : this.type
  /**
    * create a new node in the position passed
    * @param p the new position
    * @return the new node created
    */
  def movedTo(p: P) : this.type = copyNode(position = p)

  /**
    * switch on a device in the new node
    * @param d the device name
    * @return the new node created
    */
  def turnOnDevice(d: DEVICE#NAME) : this.type = {
    val selected = this.getDevice(d)
    if(selected.isDefined && !selected.get.state) {
      val newDevices = this.devices - selected.get
      val onSelected = (selected get).switchOn.asInstanceOf[DEVICE]
      return copyNode(devices = newDevices + onSelected)
    }
    this
  }

  /**
    * switch off a device in the new nodde
    * @param d the device name
    * @return the new node created
    */
  def turnOffDevice(d: DEVICE#NAME) : this.type = {
    val selected = this.getDevice(d)
    if(selected.isDefined && selected.get.state) {
      val newDevices = this.devices - selected.get
      val offSelected = (selected get).switchOff.asInstanceOf[DEVICE]
      return copyNode(devices = newDevices + offSelected)
    }
    this
  }

  /**
    * add a device in a new node
    * @param d the device
    * @return the new device created
    */
  def addDevice(d : DEVICE) : this.type = copyNode(devices = this.devices + d)

  /**
    * remove a device in a new node
    * @param d the device
    * @return the new device created
    */
  def removeDevice(d : DEVICE) : this.type = copyNode(devices = this.devices - d)
}
