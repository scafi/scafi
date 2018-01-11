package it.unibo.scafi.simulation.gui.model.core

import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Source}

/**
  * a world mutable, the observer want to see the changes in the world
  */
trait ObservableWorld extends World with Source {

  override type O = ObserverWorld
}

/**
  * generic observer of the world
  */
trait ObserverWorld extends Source#Observer

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