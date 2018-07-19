package it.unibo.scafi.simulation.gui.model.common.world.implementation.immutable

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent._
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, Source}
/**
  * a world mutable, the observer want to see the changes in the world
  */
trait AbstractObservableWorld extends World {
  self : ObservableWorld.Dependency =>
  override type O = WorldObserver
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
  def insertNode (n:NODE) : Boolean
  //TODO Aggiungere quelli possibili o se non aggiungerne nessuno?
  /**
    * add a set of node in the world
    * @param n the nodes want to add
    * @return the set of node that can't be added
    */
  def insertNodes (n : Set[NODE]): Set[NODE]

  /**
    * remove a node in the world
    * @param n the node to remove
    * @return true if the node is removed false otherwise
    */
  def removeNode(n: ID) : Boolean
  /**
    * remove a set of node in the world
    * @param n the nodes
    * @return the set of node that aren't in the wolrd
    */
  def removeNodes(n:Set[ID]) : Set[NODE]
  /**
    * remove all node in the world
    */
  def clear()

  /**
    * use strategy val to verify if the node is allowed in the world or not
    * @param n the node tested
    * @return true if the node is allowed false otherwise
    */
  protected def nodeAllowed(n:NODE) : Boolean

  class WorldObserver private[AbstractObservableWorld](listenEvent : Set[EventType]) extends Observer {
    override def update(event: Event): Unit = {
      event match {
        case WorldEvent(n,e) => if(listenEvent contains e) super.update(event)
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
  case class WorldEvent(nodes : Iterable[ID],eventType: EventType) extends Event
  //simple factory
  def createObserver(listenEvent : Set[EventType]) : O = new WorldObserver(listenEvent)
}
object AbstractObservableWorld {
  type Dependency = Source
}