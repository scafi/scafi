package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent._
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, Source}
/**
  * a world mutable, the observer want to see the changes in the world
  */
trait ObservableWorld extends World {
  self : ObservableWorld.Dependency =>
  override type O = WorldObserver

  private var _nodes : Map[ID,NODE] = Map[ID,NODE]()

  override def nodes = _nodes.values.toSet

  override def apply(node : ID) : Option[NODE] = _nodes.get(node)

  override def apply(node : Set[ID]) : Set[NODE] = _nodes.filter {y => node.contains(y._1)}.values.toSet

  final def + (n : NODE): this.type = {
    insertNode(n)
    this
  }
  final def ++ (n : Set[NODE]) : this.type = {
    insertNodes(n)
    this
  }
  final def - (n : ID): this.type = {
    removeNode(n)
    this
  }
  final def -- (n : Set[ID]): this.type = {
    removeNodes(n)
    this
  }
  /**
    * add a node in the world
    * @param n the node want to add
    * @return true if there aren't problem in addition
    *         false if : the node are already in the world
    *                    the position is not allowed
    */
  def insertNode (n:NODE) : Boolean = {
    if(_nodes contains n.id) return false
    if(!nodeAllowed(n)) return false
    _nodes += n.id -> n
    this !!! WorldEvent(Set(n.id),NodesAdded)
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
    _nodes = _nodes ++ nodeToAdd.map {x => x.id -> x}
    this !!! WorldEvent(nodeToAdd map {_.id},NodesAdded)
    return n -- nodeToAdd
  }

  /**
    * remove a node in the world
    * @param n the node to remove
    * @return true if the node is removed false otherwise
    */
  def removeNode(n: ID) : Boolean = {
    if(!(_nodes contains n)) return false
    val node = this.apply(n)
    _nodes -= n
    this !!! WorldEvent(Set(node.get.id),NodesRemoved)
    true
  }

  /**
    * remove a set of node in the world
    * @param n the nodes
    * @return the set of node that aren't in the wolrd
    */
  def removeNodes(n:Set[ID]) : Set[NODE] = {
    val nodeToRemove = n filter{x => _nodes.keySet.contains(x)}
    val nodeNotify = this.apply(nodeToRemove)
    _nodes = _nodes -- nodeToRemove
    this !!! WorldEvent(nodeNotify map {_.id},NodesRemoved)
    return this.apply(n -- nodeToRemove)
  }

  /**
    * remove all node in the world
    */
  def clear() = {
    val ids = this._nodes.keySet
    this._nodes = Map.empty
    this !!! WorldEvent(ids,NodesRemoved)
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
    _nodes = _nodes ++ n.map {x => x.id -> x}
  }

  /**
    * method call to remove nodes in te world
    * this method don't produce event like remove node
    * @param n the nodes to remove
    */
  protected def removeBeforeEvent(n : Set[ID]) = {
    _nodes = _nodes -- n
  }
  /**
    * define an observer for a world
    */
  class WorldObserver private[ObservableWorld](listenEvent : Set[EventType]) extends Observer {
    override def !!(event: Event): Unit = {
      event match {
        case WorldEvent(n,e) => if(listenEvent contains e) super.!!(event)
        case _ =>
      }
    }

    /**
      * tells the set of nodes changed
      * @return
      */
    def nodeChanged(): Set[ID] = events map {_.asInstanceOf[WorldEvent]} flatMap {_.nodes} toSet
  }

  /**
    * the event produced by world
    * @param nodes the node changed
    * @param eventType the type of event produced
    */
  case class WorldEvent(nodes: Set[ID],eventType: EventType) extends Event
  //simple factory
  def createObserver(listenEvent : Set[EventType]) : O = new WorldObserver(listenEvent)
}
object ObservableWorld {

  type Dependency = Source
}