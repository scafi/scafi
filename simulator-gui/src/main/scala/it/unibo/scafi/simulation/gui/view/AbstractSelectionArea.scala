package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Command
import it.unibo.scafi.simulation.gui.model.core.World

/**
  * define a trait used to select and clear item
  * @tparam W the world showed
  */
trait AbstractSelectionArea [W <: World]{
  self : SimulationView[W] =>

  protected var command : Option[Map[W#ID,W#P] => Command] = None
  /**
    * @return get the ids of nodes selected
    */
  def selected : Set[W#ID]

  /**
    * add a movement stategy
    * @param command the command used to move node
    */
  def addMovementAction(command : Map[W#ID,W#P] => Command) = this.command = Some(command)
}
