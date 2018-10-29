package it.unibo.scafi.simulation.frontend.view

import it.unibo.scafi.simulation.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.command.AbstractMoveCommandFactory.MultiMoveCommandFactory

/**
  * define a trait used to select and clear item
  * in a simulation view, it is used like decoration
  * pattern
  */
trait AbstractSelectionArea {
  self : SimulationView =>

  protected var argumentName : Option[String] = None

  protected var factory : Option[CommandFactory] = None
  /**
    * @return get the ids of nodes selected
    */
  def selected : Set[ID]
  /**
    * used to add movement factory, it create a move command
    * when the selection area is moved through simulation view
    * @param factory the movement factory
    * @param valueName the name of argument
    */
  def addMovementFactory(factory : MultiMoveCommandFactory, valueName : String) : Unit = {
    this.argumentName = Some(valueName)
    this.factory = Some(factory)
  }

}
