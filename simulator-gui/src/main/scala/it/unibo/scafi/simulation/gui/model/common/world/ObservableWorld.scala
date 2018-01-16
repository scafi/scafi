package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.{Node, World}
import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, Source}

/**
  * a world mutable, the observer want to see the changes in the world
  */
trait ObservableWorld extends World with Source {
  override type O = ObservableWorld.ObserverWorld

  protected var _nodes : Map[NODE#ID,NODE] = Map[NODE#ID,NODE]()

  override def nodes = _nodes.values.toSet

  override def apply(node : NODE#ID) = _nodes.get(node)

  final def + (n : NODE): this.type = {
    insertNode(n)
    this
  }
  final def ++ (n : Set[NODE]) : this.type = {
    insertNodes(n)
    this
  }
  final def - (n : NODE): this.type = {
    removeNode(n)
    this
  }
  final def -- (n : Set[NODE]): this.type = {
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
    this !!! ObservableWorld.NodeChange(n)
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
  def insertNodes (n : Set[NODE]): Boolean = {
    if(! (n forall(y => ! _nodes.contains(y.id.asInstanceOf[NODE#ID])))) return false
    if(!(n forall (y => this.nodeAllowed(y))))  return false
    _nodes = _nodes ++ n.map(x => x.id.asInstanceOf[NODE#ID] -> x)
    this !!! ObservableWorld.NodesChange(n.asInstanceOf[Set[Node]])
    true
  }

  /**
    * remove a node in the world
    * @param n the node to remove
    * @return true if the node is removed false otherwise
    */
  def removeNode(n:NODE) : Boolean = {
    if(!(_nodes contains n.id.asInstanceOf[NODE#ID])) return false
    _nodes -= n.id.asInstanceOf[NODE#ID]
    this !!! ObservableWorld.NodeChange(n)
    true
  }

  /**
    * remove a set of node in the world
    * @param n the nodes
    * @return true if all node are removed false otherwise
    */
  def removeNodes(n:Set[NODE]) : Boolean = {
    if(! (n forall(y =>  _nodes.contains(y.id.asInstanceOf[NODE#ID])))) return false
    _nodes = _nodes -- n.map(x => x.id.asInstanceOf[NODE#ID])
    this !!! ObservableWorld.NodesChange(n.asInstanceOf[Set[Node]])
    true
  }
  /**
    * use strategy val to verify if the node is allowed in the world or not
    * @param n the node tested
    * @return true if the node is allowed false otherwise
    */
  final protected def nodeAllowed(n:NODE) : Boolean = {
    if(!this.metric.positionAllowed(n.position) || this.boundary.isDefined && !boundary.get.nodeAllowed(n)) return false
    true
  }
}
object ObservableWorld {
  trait ObserverWorld extends Observer {
    private var nodesChanged : Set[Node] = Set[Node]()

    override def !!(event: Event): Unit = {
      event match {
        case NodeChange(n) => nodesChanged += n
        case NodesChange(n) => nodesChanged = nodesChanged ++ n
        case _ =>
      }
    }

    def nodeChanged : Set[Node] = this.nodesChanged

    def clearChange = this.nodesChanged = this.nodesChanged.empty
  }

  case class NodeChange(n : Node) extends Event

  case class NodesChange(n : Set[Node]) extends Event
}