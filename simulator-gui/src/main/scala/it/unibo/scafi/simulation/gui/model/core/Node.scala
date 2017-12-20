package it.unibo.scafi.simulation.gui.model.core

import it.unibo.scafi.simulation.gui.model.space.Position

/**
  * root trait of all nodes
  * all node has an ID
  */
trait Node {
  /**
    * the type of id
    */
  type ID;
  /**
    * the id associated with this node
    */
  val id : ID;
}
/**
  * a node with position in a space that could be change
  */
trait TransformNode extends Node {
  type P <: Position

  def position : P

}

/**
  * a node with a some graphics component
  */
trait GraphicsNode  extends TransformNode {
  type SHAPE <: Shape

  val shape : Shape
}

/**
  * a node that aggregate some devices
  */
trait AggregateNode extends Node {
  type DEVICE <: Device

  def devices: Set[DEVICE]

  def getDevice(name : DEVICE#NAME) : DEVICE
}


