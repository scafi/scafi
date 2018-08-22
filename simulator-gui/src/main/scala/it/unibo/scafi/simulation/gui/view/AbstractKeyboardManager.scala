package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager.AbstractKeyCode
//TODO RIMETTI A POSTO I COMMENTI
/**
  * a generic keyboard manager used to process user input
  */
trait AbstractKeyboardManager {
  self : AbstractSelectionArea =>
  /**
    * the type of library keycode
    */
  type KEY_CODE
  /**
    * key code combination
    */
  type KEYCODE_COMBINATION
  /**
    * mapping the abstract key code to real
    */
  protected var abstractToReal : Map[AbstractKeyCode, KEY_CODE] = Map.empty

  protected var abstractToCombination : Map[AbstractKeyCode,KEYCODE_COMBINATION] = Map.empty

  protected var commandArgs : Map[AbstractKeyCode,CommandArg] = Map.empty

  protected var factoryMapped : Map[AbstractKeyCode,CommandFactory] = Map.empty

  protected var valueMapped : Map[AbstractKeyCode,String] = Map.empty

  final def addCommand(code : AbstractKeyCode, arg : CommandArg, factory : CommandFactory, additionalArg : String) = {
    commandArgs += code -> arg
    valueMapped += code -> additionalArg
    factoryMapped += code -> factory
  }

  final def addCommand(code : AbstractKeyCode, arg : CommandArg, factory : CommandFactory) = {
    commandArgs += code -> arg
    factoryMapped += code -> factory
  }

}
object  AbstractKeyboardManager {

  /**
    * describe an abstract key code
    */
  sealed trait AbstractKeyCode

  /**
    * a set of code
    */
  object Code1 extends AbstractKeyCode
  object Code2 extends AbstractKeyCode
  object Code3 extends AbstractKeyCode
  object Code4 extends AbstractKeyCode
  object Code5 extends AbstractKeyCode
  object Undo extends AbstractKeyCode

}
