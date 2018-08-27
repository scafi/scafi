package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory
import it.unibo.scafi.simulation.gui.configuration.command.factory.AbstractMoveCommandFactory.MultiMoveCommandFactory

/**
  * define a trait used to select and clear item
  */
trait AbstractSelectionArea {
  self : SimulationView =>

  protected var argumentName : Option[String] = None

  protected var factory : Option[CommandFactory] = None
  /**
    * @return get the ids of nodes selected
    */
  def selected : Set[Any]
  /**
    * used to add movement factory, it create a move command
    * @param factory the movement factory
    * @param valueName the name of argument
    */
  def addMovementFactory(factory : MultiMoveCommandFactory, valueName : String) = {
    this.argumentName = Some(valueName)
    this.factory = Some(factory)
  }
}
