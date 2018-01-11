package it.unibo.scafi.simulation.gui.model.core

import it.unibo.scafi.simulation.gui.model.space.Position

/**
  * Node describe an object in a world
  */
trait Node {
  /**
    * the type of id
    */
  type ID;
  /**
    * a generic position of the node
    */
  type P <: Position
  /**
    * a shape that describe the node
    */
  type SHAPE <: Shape
  /**
    * the devices associated to the node
    */
  type DEVICE <: Device
  /**
    * the id associated with this node
    */
  val id : ID

  /**
    * @return
    *   the current position
    */
  def position : P

  /**
    *
    * @return
    *   the shape of the node
    */
  def shape : SHAPE

  /**
    * @return
    *   all devices attach on this node
    */
  def devices: Set[DEVICE]

  /**
    *
    * @param name of device
    * @return a device if it is attach on the node
    */
  def getDevice(name : DEVICE#NAME) : Option[DEVICE]
}