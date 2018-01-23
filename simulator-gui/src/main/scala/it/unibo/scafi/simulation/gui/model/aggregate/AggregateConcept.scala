package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld

/**
  * define the main concept for an aggregate world
  */
trait AggregateConcept  {
  self : ObservableWorld =>
  /**
    * the type of factory to create a node
    */
  type NODE_FACTORY <: NodeFactory
  /**
    * the type of factory to create device
    */
  type DEVICE_FACTORY<: DeviceFactory

  type NODE_PROTOTYPE <: NodePrototype

  type DEVICE_PROTOTYPE <: DevicePrototype

  type DEVICE <: AggregateDevice

  type NODE <: AggregateNode

  /**
    * root trait of node factory
    */
  trait NodeFactory {
    def create(id : self.ID,position : self.P,devices : Set[self.DEVICE], proto : NODE_PROTOTYPE) : self.NODE
    //FACTORY METHOD
    def copy[N <: AggregateNode](node : N)(position : P = node.position,
                               shape : Option[S] = node.shape,
                               devices : Set[self.DEVICE]= node.devices,
                               proto : NODE_PROTOTYPE = node.prototype) : self.NODE = {
      create(node.id,position,devices,proto)
    }
  }
  /**
    * define a skeleton of a node
    */
  trait NodePrototype {
    /**
      * @return a shape of a generic node
      */
    def shape : Option[self.S]
  }
  /**
    * the root concept of device factory
    */
  trait DeviceFactory {
    def create(n : self.NAME, s : Boolean,proto : DEVICE_PROTOTYPE) : self.DEVICE

    def copy[D <: AggregateDevice](device : D)(s : Boolean = device.state, proto : DEVICE_PROTOTYPE = device.prototype) : self.DEVICE = create(device.name,s,proto)
  }
  /**
    * define a skeleton of a device
    */

  trait DevicePrototype

  /**
    * a node in an aggregate world
    */
  trait AggregateNode extends Node {
    /**
      * @return the internal representation of node
      */
    def prototype : NODE_PROTOTYPE
  }

  trait AggregateDevice extends Device {
    /**
      * @return the internal representation of node
      */
    def prototype : DEVICE_PROTOTYPE
  }

  /**
    * the node factory of a nod
    */
  implicit val nodeFactory : NODE_FACTORY

  /**
    * the device factory
    */
  implicit val deviceFactory : DEVICE_FACTORY
}