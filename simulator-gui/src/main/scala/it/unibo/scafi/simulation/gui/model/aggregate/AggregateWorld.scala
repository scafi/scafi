package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
//TODO FIST VERSION! REMEMBER TO REFACTOR! AGGREGATE WORLD IS KEY CONCEPT
/**
  * aggregate world define a mutable world with mutable node and device
  */
trait AggregateWorld extends ObservableWorld {
  override type NODE <: AggregateNode

  /**
    * move a node in other position
    * @param n the node
    * @param p the new position
    * @throws IllegalArgumentException if the node isn't in the world
    * @return true if the movement is allowed false otherwise
    */
  //TODO THINK HOW TO MANAGE POSITION NOT ALOWED
  def moveNode(n : NODE, p : NODE#P)(implicit ev : NODE#P =:= n.P) : Boolean = {
    require(this.nodes.contains(n))
    val movedNode = n.movedTo(p).asInstanceOf[NODE]
    if(!this.nodeAllowed(movedNode))  return false
    this.removeNode(n)
    this.insertNode(movedNode)
    true
  }

  /**
    * move a set of node in a new position
    * @param nodes the map of node and new position
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the node that can't be mode
    */
  def moveNodes(nodes : Map[NODE,NODE#P]): Set[NODE] = {
    require(nodes.forall(x => this.nodes.contains(x._1)))
    val movedNodes = nodes.filter(y => this.nodes.contains(y._1))
         .map(y => y._1.movedTo(y._2.asInstanceOf[y._1.P]))
         .filter(y => !this.nodeAllowed(y.asInstanceOf[NODE]))
         .map(_.asInstanceOf[NODE])
         .toSet
    this.removeNodes(movedNodes)
    this.insertNodes(movedNodes)
    return nodes.keySet -- movedNodes
  }
  /**
    * switch on a device
    * @param n the node
    * @param name the name of device
    * @return true if the node is in the world false otherwise
    */
  def switchOnDevice(n : NODE, name : NODE#DEVICE#NAME)(implicit ev : NODE#DEVICE#NAME =:= n.DEVICE#NAME): Boolean = {
    if(!this.switching(n.turnOnDevice(name).asInstanceOf[NODE])) return false
    true
  }

  /**
    * switch on a set of device
    * @param nodes the node and the the device wants to switch on
    * @return true if all device are switch on false otherwise
    */
  def switchOnDevices(nodes : Map[NODE,NODE#DEVICE#NAME]) : Boolean = {
    val switched = nodes.map(x => x._1.turnOnDevice(x._2.asInstanceOf[x._1.DEVICE#NAME]))
                        .map(x => x.asInstanceOf[NODE])
                        .toSet
    if(!this.switchingAll(switched)) return false
    true
  }
  /**
    * switch of a device
    * @param n the node
    * @param name the name of device
    * @return true if the node is in the world false otherwise
    */
  def switchOffDevice(n : NODE, name : NODE#DEVICE#NAME)(implicit ev : NODE#DEVICE#NAME =:= n.DEVICE#NAME) : Boolean = {
    if(!this.switching(n.turnOffDevice(name).asInstanceOf[NODE])) return false
    true
  }
  /**
    * switch off a set of device
    * @param nodes the node and the the device wants to switch off
    * @return true if all device are switch on false otherwise
    */
  def switchOffDevices(nodes : Map[NODE,NODE#DEVICE#NAME]) : Boolean = {
    val switched = nodes.map(x => x._1.turnOnDevice(x._2.asInstanceOf[x._1.DEVICE#NAME]))
      .map(x => x.asInstanceOf[NODE])
      .toSet
    if(!this.switchingAll(switched)) return false
    true
  }

  /**
    * add a device to a node in the world
    * @param n the node
    * @param d the device name
    * @return true if the node is in the world false otherwise
    */
  def addDevice(n: NODE,d : NODE#DEVICE)(implicit ev : NODE#DEVICE =:= n.DEVICE) : Boolean = {
    if(!this.switching(n.addDevice(d).asInstanceOf[NODE])) return false
    true
  }

  /**
    * insert device in a set of node
    * @param nodes the nodes and the device to add
    * @return true if all the devices are added false otherwise
    */
  def addDevices(nodes : Map[NODE,NODE#DEVICE]) : Boolean = {
    val switched =  nodes.map(x => x._1.addDevice(x._2.asInstanceOf[x._1.DEVICE]))
      .map(x => x.asInstanceOf[NODE])
      .toSet
    if(!this.switchingAll(switched)) return false
    true
  }
  /**
    * remove a device in a node in the world
    * @param n the node
    * @param d the device name
    * @return true if the node is in the world false otherwise
    */
  def removeDevice(n: NODE,d : NODE#DEVICE)(implicit ev : NODE#DEVICE =:= n.DEVICE) : Boolean = {
    if(!this.switching(n.removeDevice(d).asInstanceOf[NODE])) return false
    true
  }

  /**
    * remove a device in a set of node
    * @param nodes the nodes with the device associated
    * @return true if all the devices are removed false otherwise
    */
  def removeDevices(nodes : Map[NODE,NODE#DEVICE]) : Boolean = {
    val switched =  nodes.map(x => x._1.addDevice(x._2.asInstanceOf[x._1.DEVICE]))
      .map(x => x.asInstanceOf[NODE])
      .toSet
    if(!this.switchingAll(switched)) return false
    true
  }

  private def switching(switched : NODE) : Boolean = {
    if(!this.nodes.contains(switched)) return false
    this.removeNode(switched)
    this.insertNode(switched)
  }

  private def switchingAll(switched : Set[NODE]) : Boolean = {
    if(!switched.forall(y => this.nodes.contains(y)))  return false
    this.removeNodes(switched)
    this.insertNodes(switched)
  }
}