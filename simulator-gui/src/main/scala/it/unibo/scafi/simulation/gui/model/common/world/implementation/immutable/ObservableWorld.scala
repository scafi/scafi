package it.unibo.scafi.simulation.gui.model.common.world.implementation.immutable

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.{NodesAdded, NodesRemoved}
import it.unibo.scafi.simulation.gui.model.common.world.implementation.mutable.{ObservableWorld => MutableNodeObservableWorld}
import it.unibo.scafi.simulation.gui.pattern.observer.Source

import scala.collection.mutable
//TODO METTI A POSTO I COMMENTI
/**
  * a world mutable, the observer want to see the changes in the world
  */
trait ObservableWorld extends AbstractObservableWorld {
  self : ObservableWorld.Dependency =>

  //other implementation could access to internal node structure

  private var internalMap : Map[ID,NODE] = Map[ID,NODE]()

  override def nodes = internalMap.values.toSet

  override def apply(node : ID) : Option[NODE] = internalMap.get(node)

  override def apply(node : Set[ID]) : Set[NODE] = internalMap.filter { y => node.contains(y._1)}.values.toSet
  /**
    * add a node in the world
    * @param n the node want to add
    * @return true if there aren't problem in addition
    *         false if : the node are already in the world
    *                    the position is not allowed
    */
  def insertNode (n:NODE) : Boolean = {
    if(internalMap contains n.id) return false
    if(!nodeAllowed(n)) return false
    internalMap += n.id -> n
    this notify WorldEvent(Set(n.id),NodesAdded)
    true
  }
  //TODO Aggiungere quelli possibili o se non aggiungerne nessuno?
  /**
    * add a set of node in the world
    * @param n the nodes want to add
    * @return the set of node that can't be added
    */
  def insertNodes (n : Set[NODE]): Set[NODE] = {
    val nodeToAdd = n filter {x => !nodes.contains(x) && nodeAllowed(x)}
    internalMap = internalMap ++ nodeToAdd.map { x => x.id -> x}
    this notify WorldEvent(nodeToAdd map {_.id},NodesAdded)
    return n -- nodeToAdd
  }

  /**
    * remove a node in the world
    * @param n the node to remove
    * @return true if the node is removed false otherwise
    */
  def removeNode(n: ID) : Boolean = {
    if(!(internalMap contains n)) return false
    val node = this.apply(n)
    internalMap -= n
    this notify WorldEvent(Set(node.get.id),NodesRemoved)
    true
  }

  /**
    * remove a set of node in the world
    * @param n the nodes
    * @return the set of node that aren't in the wolrd
    */
  def removeNodes(n:Set[ID]) : Set[NODE] = {
    val nodeToRemove = n filter{x => internalMap.keySet.contains(x)}
    val nodeNotify = this.apply(nodeToRemove)
    internalMap = internalMap -- nodeToRemove
    this notify WorldEvent(nodeNotify map {_.id},NodesRemoved)
    return this.apply(n -- nodeToRemove)
  }

  /**
    * remove all node in the world
    */
  def clear() : Unit = {
    val ids = this.internalMap.keySet
    this.internalMap = Map.empty
    this notify WorldEvent(ids,NodesRemoved)
  }
  /**
    * use strategy val to verify if the node is allowed in the world or not
    * @param n the node tested
    * @return true if the node is allowed false otherwise
    */
  final protected def nodeAllowed(n:NODE) : Boolean = {
    if(!this.metric.positionAllowed(n.position) ||
      this.boundary.isDefined &&
        !boundary.get.nodeAllowed(n.position,n.shape)) return false
    true
  }
  /**
    * method call to add nodes in the world, this
    * method don't produce event like insert node
    * @param n the nodes to add
    */
  protected def addBeforeEvent(n : Set[NODE]) = {
    internalMap ++= n.map { x => x.id -> x}
  }

  /**
    * method call to remove nodes in te world
    * this method don't produce event like remove node
    * @param n the nodes to remove
    */
  protected def removeBeforeEvent(n : Set[ID]) = {
    internalMap --=  n
  }
}
object ObservableWorld {

  type Dependency = Source
}