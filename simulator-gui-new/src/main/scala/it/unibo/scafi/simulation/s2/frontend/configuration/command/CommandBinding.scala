package it.unibo.scafi.simulation.s2.frontend.configuration.command

import it.unibo.scafi.simulation.s2.frontend.view.AbstractKeyboardManager
import it.unibo.scafi.simulation.s2.frontend.view.AbstractSelectionArea

/**
 * a strategy used to map command
 */
trait CommandBinding {
  /**
   * map the command with the main input policy
   * @param keyboard
   *   keyboard manager
   * @param selection
   *   selection manager
   */
  def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea] = None): Unit
}
