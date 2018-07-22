package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Command
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager.AbstractKeyCode

/**
  * a generic keyboard manager used to process user input
  * @tparam W the type of world showed
  */
trait AbstractKeyboardManager [W <: World]{
  self : AbstractSelectionArea[W] =>
  /**
    * the type of library keycode
    */
  type KEYCODE
  /**
    * mapping the abstract key code to real
    */
  protected var abstractToReal : Map[AbstractKeyCode, KEYCODE] = Map.empty
  /**
    * a map that associate the keyboard code with the command to process
    */
  protected var commands : Map[AbstractKeyCode,Set[Any] => Command] = Map.empty

  /**
    * add a command to execute
    * @param code the code of keyboard
    * @param command a command to execute
    */
  final def addCommand(code : AbstractKeyCode, command : Set[Any] => Command) = commands += code -> command

}
object  AbstractKeyboardManager {

  /**
    * describe an abstract key code
    */
  trait AbstractKeyCode

  /**
    * a set of code
    */
  object Code1 extends AbstractKeyCode
  object Code2 extends AbstractKeyCode
  object Code3 extends AbstractKeyCode
  object Code4 extends AbstractKeyCode
  object Code5 extends AbstractKeyCode
}
