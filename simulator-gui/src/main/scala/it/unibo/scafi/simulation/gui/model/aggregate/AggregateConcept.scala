package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.EventType
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
import it.unibo.scafi.simulation.gui.pattern.observer.Event

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
  /**
    * an event produced when a device attached in a node changed
    * @param id the node id
    * @param name the device name
    * @param eventType the type of event
    */
  case class DeviceEvent(id : ID, name : NAME, eventType: EventType) extends Event

  /**
    * an obsever that want to see device changes
    * @param events
    */
  protected class AggregateWorldObserver(events : Set[EventType]) extends WorldObserver(events) {
    private var devs : Map[ID,Set[NAME]] = Map()
    override def update(event: Event): Unit = {
      event match {
        case DeviceEvent(id,name,e) => if(listenEvent contains e) {
          ids += id
          if(devs.get(id).isEmpty) devs += id -> Set()
          devs += id -> (devs.get(id).get + name)
        }
        case _ => super.update(event)
      }
    }

    override def nodeChanged(): Set[ID] = {
      devs = devs.empty
      super.nodeChanged()
    }

    override def clear(): Unit = {
      devs = devs.empty
      super.clear()
    }

    def deviceChanged() : Map[ID,Set[NAME]] = {
      val res = this.devs
      this.clear()
      res
    }
  }
}

object AggregateConcept {
  type Dependency = ObservableWorld
}
