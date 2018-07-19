package it.unibo.scafi.simulation.gui.model.aggregate.implementation.mutable

import it.unibo.scafi.simulation.gui.model.common.world.implementation.mutable.ObservableWorld

/**
  * describe a set of aggregate concept used to define an aggregate world
  */
trait AggregateConcept {
  self : AggregateConcept.Dependency =>

  protected type MUTABLE_NODE <: AggregateMutableNode

  protected type MUTABLE_DEVICE <: RootMutableDevice

  type DEVICE_PRODUCER <: DeviceProducer

  /**
    * this trait describe a mutable node, it must be protected because
    * the node state can be changed only by world.
    * outside the client can only see the node state
    */
  protected trait AggregateMutableNode extends RootMutableNode {
    /**
      * the position on node is variable
      */
    var position : P

    /**
      * add new device associated with this node
      * @param device the new device to add
      */
    def addDevice(device : MUTABLE_DEVICE) : Boolean

    /**
      * remove the device with the name associated
      * @param name
      */
    def removeDevice(name : NAME) : Boolean

    /**
      * get the mutable representation of the device
      * @param name device name
      * @return None if the device isn't attach on the node Some of device otherwise
      */
    def getMutableDevice(name : NAME) : Option[MUTABLE_DEVICE]
  }

  /**
    * the root trait of all mutable device
    */
  protected trait RootMutableDevice extends Device {
    /**
      * create a view of mutable device and return it
      * @return the view of device
      */
    def view : DEVICE
  }

  /**
    * a trait used like a strategy to create device with some structure
    */
  trait DeviceProducer {
    /**
      * create a representation of mutable device
      * @return the representation created
      */
    def build : MUTABLE_DEVICE
  }

  /**
    * a trait used to create a sequence of node
    */
  trait FactoryNodeProducer extends RootNodeProducer {
    def buildAll() : Iterable[MUTABLE_NODE]
  }
}

object AggregateConcept {
  type Dependency = ObservableWorld
}
