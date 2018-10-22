package it.unibo.scafi.simulation.gui.model.aggregate

/**
  * a skeleton of node in a aggregate world
  */
trait AbstractNodeDefinition extends AggregateConcept {
  self: AbstractNodeDefinition.Dependency =>
  /**
    * skeleton of a mutable node
    * @param id the node id
    * @param position the initial node position
    * @param shape the node shape
    */
  abstract class AbstractMutableNode(val id : ID, var position: P, val shape : Option[S]) extends AggregateMutableNode {
    /**
      * the internal representation of device collection
      */
    protected var devs : Map[NAME,MUTABLE_DEVICE] = Map.empty

    override def addDevice(device: MUTABLE_DEVICE): Boolean = {
      if(devs.contains(device.name)) {
        false
      } else {
        devs += device.name -> device
        true
      }
    }

    override def removeDevice(name: NAME): Boolean = {
      if(devs.contains(name)) {
        devs -= name
        true
      } else {
        false
      }
    }

    override def devices: Set[DEVICE] = devs.values.map{_.view}.toSet

    override def getDevice(name: NAME): Option[DEVICE] = {
      val dev = devs.get(name)
      if(dev.isEmpty) {
        None
      } else {
        Some(dev.get.view)
      }
    }

    def getMutableDevice(name : NAME) : Option[MUTABLE_DEVICE] = devs.get(name)

    def canEqual(other: Any): Boolean = other.isInstanceOf[AbstractMutableNode]

    override def equals(other: Any): Boolean = other match {
      case that: AbstractMutableNode =>
        (that canEqual this) &&
          id == that.id
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(id)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }

  /**
    * abstract node builder used to create a single instance of node
    * @param id the node id
    * @param shape the node shape
    * @param position the node position
    * @param deviceProducer a set of device producer
    */
  abstract class AbstractNodeBuilder(val id : ID,
                                     val shape : Option[S] = None,
                                     val position : P,
                                     val deviceProducer : Iterable[DEVICE_PRODUCER] = Set.empty
                                    ) extends RootNodeProducer

}

object AbstractNodeDefinition {
  type Dependency = AggregateConcept.Dependency
}