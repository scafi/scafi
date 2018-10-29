package it.unibo.scafi.simulation.frontend.configuration.command

import it.unibo.scafi.simulation.frontend.view.{AbstractKeyboardManager, AbstractSelectionArea}

/**
  * a strategy used to map command
  */
trait CommandBinding {
  /**
    * map the command with the main input policy
    * @param keyboard keyboard manager
    * @param selection selection manager
    */
  def apply(keyboard : AbstractKeyboardManager, selection : Option[AbstractSelectionArea] = None)
}
