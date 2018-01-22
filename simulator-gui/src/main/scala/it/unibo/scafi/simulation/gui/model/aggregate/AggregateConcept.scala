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

  /**
    * root trait of node factory
    */
  trait NodeFactory {
    def create(id : self.ID,position : self.P, shape : Option[self.S],devices : Set[self.DEVICE]) : self.NODE
    //FACTORY METHOD
    def copy(node : self.NODE)(position : P = node.position, shape : Option[S] = node.shape, devices : Set[self.DEVICE]= node.devices) : self.NODE = {
      create(node.id,position,shape,devices)
    }
  }

  /**
    * the root concept of device factory
    */
  trait DeviceFactory {
    def create(n : self.NAME, s : Boolean, node : Option[self.NODE]) : self.DEVICE

    def copy(device : self.DEVICE)(s : Boolean = device.state) : self.DEVICE = create(device.name,s,device.node)
  }

  /**
    * @return the node factory used to create nodes
    */
  def nodeFactory : NODE_FACTORY

  /**
    * @return the device factory used to create devices
    */
  def deviceFactory : DEVICE_FACTORY
}