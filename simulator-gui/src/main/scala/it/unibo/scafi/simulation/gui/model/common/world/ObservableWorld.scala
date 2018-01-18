package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent._
import it.unibo.scafi.simulation.gui.model.core.{Node, World}
import it.unibo.scafi.simulation.gui.pattern.observer.{Observer, Source}
/**
  * a world mutable, the observer want to see the changes in the world
  */
//TODO pensa se effettuare una suddivisione degli eventi più fine o meno
trait ObservableWorld extends World{
  this : ObservableWorld.Dependency =>
  override type O <: ObservableWorld.WorldObserver[NODE]

  private var _nodes : Map[NODE#ID,NODE] = Map[NODE#ID,NODE]()

  override def nodes = _nodes.values.toSet

  override def apply(node : NODE#ID) : Option[NODE] = _nodes.get(node)

  override def apply(node : Set[NODE#ID]) : Set[NODE] = _nodes.filter {y => node.contains(y._1)}.values.toSet

  final def + (n : NODE): this.type = {
    insertNode(n)
    this
  }
  final def ++ (n : Set[NODE]) : this.type = {
    insertNodes(n)
    this
  }
  final def - (n : NODE#ID): this.type = {
    removeNode(n)
    this
  }
  final def -- (n : Set[NODE#ID]): this.type = {
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
    if(_nodes contains n.id.asInstanceOf[NODE#ID]) return false
    if(!nodeAllowed(n)) return false
    _nodes += n.id.asInstanceOf[NODE#ID] -> n
    this !!! NodesAdded(Set(n))
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
    _nodes = _nodes ++ nodeToAdd.map {x => x.id.asInstanceOf[NODE#ID] -> x}
    this !!! NodesAdded(nodeToAdd)
    return n -- nodeToAdd
  }

  /**
    * remove a node in the world
    * @param n the node to remove
    * @return true if the node is removed false otherwise
    */
  def removeNode(n:NODE#ID) : Boolean = {
    if(!(_nodes contains n)) return false
    val node = this.apply(n)
    _nodes -= n
    this !!! NodesRemoved(Set(node.get))
    true
  }

  /**
    * remove a set of node in the world
    * @param n the nodes
    * @return the set of node that aren't in the wolrd
    */
  def removeNodes(n:Set[NODE#ID]) : Set[NODE] = {
    val nodeToRemove = n filter{x => _nodes.keySet.contains(x)}
    val nodeNotify = this.apply(nodeToRemove)
    _nodes = _nodes -- nodeToRemove
    this !!! NodesRemoved(nodeNotify)
    return this.apply(n -- nodeToRemove)
  }
  /**
    * use strategy val to verify if the node is allowed in the world or not
    * @param n the node tested
    * @return true if the node is allowed false otherwise
    */
  final protected def nodeAllowed(n:NODE) : Boolean = {
    if(!this.metric.positionAllowed(n.position.asInstanceOf[NODE#P]) || this.boundary.isDefined && !boundary.get.nodeAllowed(n)) return false
    true
  }

  /**
    * method call to add nodes in the world, this
    * method don't produce event like insert node
    * @param n the nodes to add
    */
  protected def addBeforeEvent(n : Set[NODE]) = {
    _nodes = _nodes ++ n.map {x => x.id.asInstanceOf[NODE#ID] -> x}
  }

  /**
    * method call to remove nodes in te world
    * this method don't produce event like remove node
    * @param n the nodes to remove
    */
  protected def removeBeforeEvent(n : Set[NODE#ID]) = {
    _nodes = _nodes -- n
  }
}
object ObservableWorld {
  type Dependency = Source

  /**
    * the root class of all world observer
    */
  trait WorldObserver[N <: Node] extends Observer {
    def nodeChanged(): Set[N]
  }

  /**
    * observer all world change and store the node modified
    */
  trait AllChangesObserver[N <: Node] extends WorldObserver[N] {
     override def nodeChanged() : Set[N] = {
       var res = Set[N]()
       for(event <- events) {
         val x = event match {
           case NodesChangeEvent(n) => res = res ++ n.asInstanceOf[Set[N]]
           case _ =>
         }
       }
       res
     }
  }
}