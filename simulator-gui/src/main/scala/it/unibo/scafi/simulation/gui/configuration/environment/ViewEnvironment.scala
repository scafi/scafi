package it.unibo.scafi.simulation.gui.configuration.environment

import it.unibo.scafi.simulation.gui.view._

/**
  * a view environment, describe main thing attached on view concept
  */
trait ViewEnvironment[V <: View] {
  /**
    * describe window parameter
    * @return a window configuration
    */
  def windowConfiguration : WindowConfiguration

  /**
    * used to set the window configuration
    * @param configuration the configuration to set
    */
  def windowConfiguration_=(configuration: WindowConfiguration) : Unit
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
    * init the view environment
    */
  def init()
}
