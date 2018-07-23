package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Command
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.space.Point3D

/**
  * define a trait used to select and clear item
  */
trait AbstractSelectionArea {
  self : SimulationView =>

  protected var command : Option[Map[Any,Point3D] => Command] = None
  /**
    * @return get the ids of nodes selected
    */
  def selected : Set[Any]

  /**
    * add a movement strategy
    * @param command the command used to move node
    */
  def addMovementAction(command : Map[Any,Point3D] => Command) = this.command = Some(command)
}
