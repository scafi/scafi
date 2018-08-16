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
  type KEYCODE
  /**
    * mapping the abstract key code to real
    */
  protected var abstractToReal : Map[AbstractKeyCode, KEYCODE] = Map.empty
  /**
    * a map that associate the keyboard code with the command to process, has id in input and a value
    */
  protected var idsValueCommands : Map[AbstractKeyCode,(Any,Set[Any]) => CommandArg] = Map.empty

  /**
    * a map that associate the keyboard code with the command to process, has id in input
    */
  protected var idsCommands : Map[AbstractKeyCode,(Set[Any]) => CommandArg] = Map.empty
  /**
    * a map that associate the keyboard code with the command to process
    */
  protected var genericCommands : Map[AbstractKeyCode,(Any) => CommandArg] = Map.empty

  protected var factoryMapped : Map[AbstractKeyCode,CommandFactory] = Map.empty

  protected var valueMapped : Map[AbstractKeyCode,Any] = Map.empty
  /**
    * add a command to execute
    * @param code the code of keyboard
    * @param command a command to execute
    */
  final def addCommand(code : AbstractKeyCode, command : (Any,Set[Any]) => CommandArg, factory : CommandFactory, value : Any) = {
    idsValueCommands += code -> command
    valueMapped += code -> value
    factoryMapped += code -> factory
  }

  /**
    * add a command to execute
    * @param code the code of keyboard
    * @param command a command to execute
    */
  final def addCommand(code : AbstractKeyCode, command : (Set[Any]) => CommandArg, factory : CommandFactory) = {
    idsCommands += code -> command
    factoryMapped += code -> factory
  }

  final def addCommand(code : AbstractKeyCode, command : (Any) => CommandArg, factory : CommandFactory, value : Any) = {
    genericCommands += code -> command
    valueMapped += code -> value
    factoryMapped += code -> factory
  }

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
