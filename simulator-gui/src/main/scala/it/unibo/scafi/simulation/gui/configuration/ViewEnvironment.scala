package it.unibo.scafi.simulation.gui.configuration

import it.unibo.scafi.simulation.gui.view._

/**
  * a view environment, describe main thing attached on view concept
  */
trait ViewEnvironment[V <: View] {
  /**
    * @return keyboard manager attached on view
    */
  def keyboard : AbstractKeyboardManager

  /**
    * @return selection manager (if it is attached on view)
    */
  def selection : Option[AbstractSelectionArea]

  /**
    * the container of view
    * @return
    */
  def container : Container[V]

  /**
    * initialize the environment
    */
  def init()
}
