package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Command
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.space.Point

/**
  * define a trait used to select and clear item
  */
trait AbstractSelectionArea [ID <: World#ID,P <: Point]{
  protected var command : Option[Map[ID,P] => Command] = None
  /**
    * @return get the ids of nodes selected
    */
  def selected : Set[ID]

  /**
    * add a movement stategy
    * @param command the command used to move node
    */
  def addMovementAction(command : Map[ID,P] => Command) = this.command = Some(command)
}
