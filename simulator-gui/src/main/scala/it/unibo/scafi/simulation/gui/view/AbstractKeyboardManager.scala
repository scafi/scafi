package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Command
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager.AbstractKeyCode

trait AbstractKeyboardManager [ID <: World#ID]{
  self : AbstractSelectionArea[ID,_] =>
  type KEYCODE
  protected var abstractToReal : Map[AbstractKeyCode, KEYCODE] = Map.empty
  protected var commands : Map[AbstractKeyCode,Set[ID] => Command] = Map.empty
  final def addCommand(code : AbstractKeyCode, command : Set[ID] => Command) = commands += code -> command

}
object  AbstractKeyboardManager {
  trait AbstractKeyCode

  object Code1 extends AbstractKeyCode
  object Code2 extends AbstractKeyCode
  object Code3 extends AbstractKeyCode
  object Code4 extends AbstractKeyCode
  object Code5 extends AbstractKeyCode
}
