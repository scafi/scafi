package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Source}

/**
  * a world mutable, the observer want to see the changes in the world
  */
trait ObservableWorld extends World with Source {

  override type O = ObserverWorld

  /**
    * generic observer of the world
    */
  trait ObserverWorld extends Observer

  private var _nodes : Map[NODE#ID,NODE] = Map[NODE#ID,NODE]()

  override def nodes = _nodes.values.toSet

  override def apply(node : NODE#ID) = _nodes.get(node)

  def ++ (n : NODE): ObservableWorld = {
    val id : NODE#ID = n.id.asInstanceOf[NODE#ID]
    _nodes += n.id.asInstanceOf[NODE#ID] -> n
    this
  }

  /**
    * change the position of node in the world
    * @param node that what to change the position
    * @param position new position of the node
    * @return true if the node moved false otherwise
    */
  def changePosition(id:NODE#ID, position: NODE#P) : Boolean

  /**
    * change the position of a set of node
    * @param nodes that want to change the position
    * @return the nodes that can't moved empty otherwise
    */
  def changePosition(nodes : Map[NODE#ID,NODE#P]) : Set[NODE#ID]

}
object ObservableWorld {
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
}