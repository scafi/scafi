package it.unibo.scafi.simulation

import it.unibo.scafi.simulation.SimulationObserver.MovementEvent
import it.unibo.utils.observer.{Event, Observer}

class SimulationObserver[ID] extends Observer {
  private var idMovedInternal = Set.empty[ID]
  def idMoved : Set[ID] = {
    val res = idMovedInternal
    idMovedInternal = Set.empty
    res
  }

  override def update(event: Event): Unit = event match {
    case MovementEvent(id) => idMovedInternal += id.asInstanceOf[ID]
  }
}

object SimulationObserver {
  case class MovementEvent(id : Any) extends Event
}