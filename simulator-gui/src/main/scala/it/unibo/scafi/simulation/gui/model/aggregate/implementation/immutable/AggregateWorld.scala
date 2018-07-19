package it.unibo.scafi.simulation.gui.model.aggregate.implementation.immutable

import it.unibo.scafi.simulation.gui.model.aggregate.AbstractAggregateWorld
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodesDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.implementation.immutable.ObservableWorld
/**
  * aggregate world define a mutable world with mutable node and device
  */
//TODO CREATE AN OBSERVER THAT COULD BE OBSERVER DEVICE EVENT
trait AggregateWorld extends ObservableWorld with AbstractAggregateWorld with AggregateConcept {
  this : AggregateWorld.Dependency =>

  def moveNode(n : ID, p : P) : Boolean = {
    val node = this.getNodeOrThrows(n)
    produceResult(node,p ,filterPosition(node,p),a => WorldEvent(Set(a.id),NodesMoved))
  }


  def moveNodes(nodes : Map[ID,P]): Set[NODE] = {
    produceResult(nodes,filterPosition,a => WorldEvent(a map {_.id},NodesMoved))
  }
  def addDevice(n: ID,d : DEVICE): Boolean = {
    val node = getNodeOrThrows(n)
    val nodeChanged  = toggleDevice(node,d,true)
    produceResult(node,d,nodeChanged,a => WorldEvent(Set(a.id),NodesDeviceChanged))
  }

  def addDevices(nodes : Map[ID,DEVICE]) : Set[NODE] = {
    produceResult(nodes,(a,b : DEVICE) => toggleDevice(a,b,true),a => WorldEvent(a map {_.id},NodesDeviceChanged))
  }

  def removeDevice(n: ID,d : DEVICE): Boolean = {
    val node = getNodeOrThrows(n)
    val nodeChanged = toggleDevice(node,d,false)
    produceResult(node,d, nodeChanged , a => WorldEvent(Set(a.id),NodesDeviceChanged))
  }

  def removeDevices(nodes : Map[ID,DEVICE]) : Set[NODE] = {
    produceResult(nodes,(a,b : DEVICE) => toggleDevice(a,b,false),a => WorldEvent(a map {_.id},NodesDeviceChanged))
  }

  // Some utility method
  private def switching(switched : Set[NODE]) : Unit = {
    this.removeBeforeEvent(switched.map(_.id))
    this.addBeforeEvent(switched)
  }
  //take a map of id and value, using filter change the vlaue
  private def produceResult[A](map : Map[ID,A],
                               filter :(NODE,A) => Option[NODE],
                               producer : (Set[NODE]) => WorldEvent): Set[NODE] = {
    require(map.keySet.forall(this.apply(_).isDefined))
    val switched = map.map(x => this.apply(x._1).get -> x._2)
      .map {x => filter(x._1,x._2)}
      .filter(_.isDefined)
      .map(_.get)
      .toSet
    switching(switched)
    this.notify(producer(switched))
    this.apply(map.keySet) -- switched
  }

  private def produceResult[A](n : NODE, v : A,
                               filter : => Option[NODE],
                               producer : NODE => WorldEvent): Boolean = {
    val node = filter
    if(node.isEmpty) return false
    switching(Set(node.get))
    this.notify(producer(node.get))
    true
  }

  private def getNodeOrThrows(id : ID) : NODE = {
    require(this.apply(id).isDefined)
    return this.apply(id).get
  }
  private val filterPosition : (NODE,P) => Option[NODE] = (a,b) => {
    val moved = nodeFactory.copy(a)(position = b)
    val res : Option[NODE] = if(!this.nodeAllowed(moved)) None else Some(moved)
    res
  }

  private val toggleDevice: (NODE,DEVICE,Boolean) => Option[NODE] = (node,dev,toAdd) => {
    if(node.getDevice(dev.name).isDefined == toAdd) None
    var res : NODE = node
    if(toAdd) {
      res = nodeFactory.copy(node)(devices = node.devices + dev)
    } else {
      res = nodeFactory.copy(node)(devices = node.devices - dev)
    }
    Some(res)
  }
}
object AggregateWorld {
  type Dependency = ObservableWorld.Dependency
}