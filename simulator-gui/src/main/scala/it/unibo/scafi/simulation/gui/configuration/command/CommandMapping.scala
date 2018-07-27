package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.view.{AbstractKeyboardManager, AbstractSelectionArea}

/**
  * a strategy used to map command
  */
trait CommandMapping {
  /**
    * map the command with the main input policy
    * @param keyboard keyboard manager
    * @param selection selection manager
    */
  def map(keyboard : AbstractKeyboardManager, selection : Option[AbstractSelectionArea] = None)
}
