package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, Source}

/**
  * a world mutable, the observer want to see the changes in the world
  */
trait ObservableWorld extends World with Source {

  override type O = ObservableWorld.ObserverWorld

  private var _nodes : Map[NODE#ID,NODE] = Map[NODE#ID,NODE]()

  override def nodes = _nodes.values.toSet

  override def apply(node : NODE#ID) = _nodes.get(node)

  //TODO -> provocare un eccezione?
  /**
    * add a node in the world
    * @param n the node want to add
    * @return true if there aren't problem in addition
    *         false if : the node are already in the world
    *                    the position is not allowed
    */
  def + (n : NODE): Boolean = {
    if(_nodes contains n.id.asInstanceOf[NODE#ID]) return false
    if(!this.metric.positionAllowed(n.position)) return false
    if(this.boundary.isDefined && !(boundary.get.nodeAllowed(n))) return false
    _nodes += n.id.asInstanceOf[NODE#ID] -> n
    this !!! ObservableWorld.NodeAdded(n)
    true
  }
  //TODO Aggiungere quelli possibili o se non aggiungerne nessuno?
  /**
    * add a set of node in the world
    * @param n the nodes want to add
    * @return true if there aren't no problem in addition
    *         false if: almost one node are already in the world
    *                   almost one position is not allowed
    */
  def ++ (n : Set[NODE]) : Boolean = {
    if(! (n forall(y => ! _nodes.contains(y.id.asInstanceOf[NODE#ID])))) return false
    if(!(n forall (y => this.metric.positionAllowed(y.position))))  return false
    if(this.boundary.isDefined) {
      val bound = this.boundary.get
      if(!(n forall (bound.nodeAllowed(_))))  return false
    }
    _nodes = _nodes ++ n.map(x => x.id.asInstanceOf[NODE#ID] -> x)
    this !!! ObservableWorld.NodesAdded(n.asInstanceOf[Set[ObservableWorld#NODE]])
    true
  }

  /**
    * change the position of node in the world
    * @param node that what to change the position
    * @param position new position of the node
    * @return true if the node moved false otherwise
    */
  def changePosition(node:NODE, position: NODE#P) : Boolean

  /**
    * change the position of a set of node
    * @param nodes that want to change the position
    * @return the nodes that can't moved empty otherwise
    */
  def changePosition(nodes : Map[NODE,NODE#P]) : Set[NODE]

}
object ObservableWorld {
  /**
    * generic observer of the world
    */
  trait ObserverWorld extends Observer

  /**
    *
    * @param p
    * @param n
    */
  case class PositionChanged(p: ObservableWorld#NODE#P,n : ObservableWorld#NODE) extends Event

  /**
    *
    * @param nodes
    */
  case class PositionsChanged(nodes: Map[ObservableWorld#NODE,ObservableWorld#NODE#P]) extends Event

  /**
    *
    * @param node
    */
  case class NodeAdded(node: ObservableWorld#NODE) extends Event

  /**
    *
    * @param nodes
    */
  case class NodesAdded(nodes : Set[ObservableWorld#NODE]) extends Event
}