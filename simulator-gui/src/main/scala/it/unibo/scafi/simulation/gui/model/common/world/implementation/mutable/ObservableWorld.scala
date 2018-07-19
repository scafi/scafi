package it.unibo.scafi.simulation.gui.model.common.world.implementation.mutable

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.EventType
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, Source}

/**
  * describe a mutable world with the possibility to add or remove node
  */
trait ObservableWorld extends World with CommonDefinition {
  self: ObservableWorld.Dependency =>
  override type O = WorldObserver

  /**
    * method used to insert node produced by the instance passed
    * @param producer the produced of node
    * @return true if the node is legit (the position is in the world...) false otherwise
    */
  def insertNode(producer : NODE_PRODUCER) : Boolean

  /**
    * method used to remove a node by his id
    * @param id the id of node that the user want to remove
    * @return true if the node is in the world false otherwise
    */
  def removeNode(id : ID) : Boolean

  /**
    * remove all node in the world
    */
  def clear()
  /**
    * use strategy val to verify if the node is allowed in the world or not
    * @param n the node tested
    * @return true if the node is allowed false otherwise
    */
  protected def nodeAllowed(n:MUTABLE_NODE) : Boolean

  class WorldObserver private[ObservableWorld](listenEvent : Set[EventType]) extends Observer {
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

object ObservableWorld {
  type Dependency = Source
}