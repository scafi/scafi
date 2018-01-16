package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld
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
  def moveNode(n : NODE#ID, p : NODE#P) : Boolean = {
    val node = this.getNodeOrThrows(n)
    produceResult(node,p ,filterPosition(node,p))
  }

  /**
    * move a set of node in a new position
    * @param nodes the map of node and new position
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the node that can't be mode
    */
  def moveNodes(nodes : Map[NODE#ID,NODE#P]): Set[NODE] = {
    produceResult(nodes,filterPosition)
  }
  private val filterPosition : (NODE,NODE#P) => Option[NODE] = (a,b) => {
    val moved = a.movedTo(b.asInstanceOf[a.P]).asInstanceOf[NODE]
    val res = if(!this.nodeAllowed(moved)) None else Some(moved)
    res.asInstanceOf[Option[NODE]]
  }
  /**
    * switch on a device
    * @param n the node
    * @param name the name of device
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the device turn on false otherwise
    */
  def switchOnDevice(n : NODE#ID, name : NODE#DEVICE#NAME): Boolean = {
    val node = getNodeOrThrows(n)
    produceResult(node,name ,node.turnOnDevice(name.asInstanceOf[node.DEVICE#NAME]).asInstanceOf[Option[NODE]])
  }

  /**
    * switch on a set of device
    * @param nodes the node and the the device wants to switch on
    * @throws IllegalArgumentException if the some node aren't in world
    * @return true if all device are switch on false otherwise
    */
  def switchOnDevices(nodes : Map[NODE#ID,NODE#DEVICE#NAME]) : Set[NODE] = {
    produceResult(nodes,(a,b : NODE#DEVICE#NAME) => a.turnOnDevice(b.asInstanceOf[a.DEVICE#NAME]).asInstanceOf[Option[NODE]])
  }
  /**
    * switch of a device
    * @param n the node
    * @param name the name of device
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the device is switched off false otherwise
    */
  def switchOffDevice(n : NODE#ID, name : NODE#DEVICE#NAME): Boolean = {
    val node = getNodeOrThrows(n)
    produceResult(node,name,node.turnOffDevice(name.asInstanceOf[node.DEVICE#NAME]).asInstanceOf[Option[NODE]])
  }
  /**
    * switch off a set of device
    * @param nodes the node and the the device wants to switch off
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the set of node that can't turn on a device
    */
  def switchOffDevices(nodes : Map[NODE#ID,NODE#DEVICE#NAME]) : Set[NODE] = {
    produceResult(nodes,(a,b: NODE#DEVICE#NAME) => a.turnOffDevice(b.asInstanceOf[a.DEVICE#NAME]).asInstanceOf[Option[NODE]])
  }

  /**
    * add a device to a node in the world
    * @param n the node
    * @param d the device name
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the node is in the world false otherwise
    */
  def addDevice(n: NODE#ID,d : NODE#DEVICE): Boolean = {
    val node = getNodeOrThrows(n)
    produceResult(node,d,node.addDevice(d.asInstanceOf[node.DEVICE]).asInstanceOf[Option[NODE]])
  }

  /**
    * insert device in a set of node
    * @param nodes the nodes and the device to add
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the set of node that can't add a device
    */
  def addDevices(nodes : Map[NODE#ID,NODE#DEVICE]) : Set[NODE] = {
    produceResult(nodes,(a,b : NODE#DEVICE) => a.addDevice(b.asInstanceOf[a.DEVICE]).asInstanceOf[Option[NODE]])
  }
  /**
    * remove a device in a node in the world
    * @param n the node
    * @param d the device name
    * @throws IllegalArgumentException if the node isn't in world
    * @return true if the node is in the world false otherwise
    */
  def removeDevice(n: NODE#ID,d : NODE#DEVICE): Boolean = {
    val node = getNodeOrThrows(n)
    produceResult(node,d, node.removeDevice(d.asInstanceOf[node.DEVICE]).asInstanceOf[Option[NODE]])
  }

  /**
    * remove a device in a set of node
    * @param nodes the nodes with the device associated
    * @throws IllegalArgumentException if some node aren't in the world
    * @return the set of node that can't remove the device
    */
  def removeDevices(nodes : Map[NODE#ID,NODE#DEVICE]) : Set[NODE] = {
    produceResult(nodes,(a,b : NODE#DEVICE) => a.removeDevice(b.asInstanceOf[a.DEVICE]).asInstanceOf[Option[NODE]])
  }

  private def switching(switched : NODE) : Unit = {
    this.removeNode(switched.id.asInstanceOf[NODE#ID])
    this.insertNode(switched)
  }
  private def produceResult[A](map : Map[NODE#ID,A], filter :(NODE,A) => Option[NODE]): Set[NODE] = {
    require(map.keySet.forall(this(_).isDefined))
    val switched = map.map(x => this(x._1).get.asInstanceOf[NODE] -> x._2)
      .map(x => filter(x._1,x._2))
      .filter(_.isDefined)
      .map(_.get)
      .toSet
    this.removeNodes(switched.map(_.id.asInstanceOf[NODE#ID]))
    this.insertNodes(switched)
    this.apply(map.keySet) -- switched
  }

  private def produceResult[A](n : NODE, v : A, filter : => Option[NODE]): Boolean = {
    val node = filter
    if(node.isEmpty) return false
    switching(node.get)
    true
  }

  private def getNodeOrThrows(id : NODE#ID) : NODE = {
    require(this(id).isDefined)
    return this(id).get
  }
}