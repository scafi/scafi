package it.unibo.scafi.simulation.s2.frontend.view

import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.s2.frontend.view.AbstractKeyboardManager.AbstractKeyCode
/**
 * a generic keyboard manager used to process user input
 */
trait AbstractKeyboardManager {
  self: AbstractSelectionArea =>

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
  protected var abstractToReal: Map[AbstractKeyCode, KEY_CODE] = Map.empty
  /**
   * associate an abstract key code to a key combination
   */
  protected var abstractToCombination: Map[AbstractKeyCode, KEYCODE_COMBINATION] = Map.empty
  /**
   * associate an abstract key code to an argument and a factory used to pass at a run time machine to process input
   * argument
   */
  protected var commandArgs: Map[AbstractKeyCode, (CommandFactory, CommandArg)] = Map.empty
  /**
   * a map used to add another argument to pass at runtime machine
   */
  protected var valueMapped: Map[AbstractKeyCode, String] = Map.empty

  /**
   * this method allow to link an abstract key code to some command creation with an additional command arg used to set
   * at run time with information retrieve with {@see AbstractSelectionArea}
   * @param code
   *   the abstract key code
   * @param arg
   *   the command argument used to factory
   * @param factory
   *   the factory that produced command
   * @param additionalArg
   *   the additional argument used to pass at command factory
   */
  final def linkCommandCreation(
      code: AbstractKeyCode,
      arg: CommandArg,
      factory: CommandFactory,
      additionalArg: String
  ): Unit = {
    commandArgs += code -> (factory, arg)
    valueMapped += code -> additionalArg
  }

  /**
   * this method allow to link an abstract key code to some command creation
   * @param code
   *   the abstract key code
   * @param arg
   *   the command argument used to factory
   * @param factory
   *   the factory that produced command
   */
  final def linkCommandCreation(code: AbstractKeyCode, arg: CommandArg, factory: CommandFactory): Unit =
    commandArgs += code -> (factory, arg)
  /**
   * tell foreach command the action associated
   */
  final def commandDescription: String = toDescription(abstractToReal) + "\n" + toDescription(abstractToCombination)

  /*
   * A method used to create help
   * */
  private def toDescription[A](map: Map[AbstractKeyCode, A]): String = map.map { x =>
    x._2.toString.toLowerCase() -> x._1
  }.filter(x => commandArgs.contains(x._2))
    .map(x => commandArgs(x._2) -> x._1)
    .map { x =>
      s"${x._2} ${x._1._1.description} ( ${x._1._2.mkString(":")})"
    }
    .mkString("\n")
}
object AbstractKeyboardManager {

  /**
   * describe an abstract key code
   */
  sealed trait AbstractKeyCode

  /**
   * a set of code when you add code here you must remember to add a mapping in KeyCodeManager, for example in a javafx
   * implementation if you add here Code7 you must write something like this: <pre> {@code abstractToReal += Code7 ->KeyCode.Digit7 } </pre>
   */
  case object Code1 extends AbstractKeyCode
  case object Code2 extends AbstractKeyCode
  case object Code3 extends AbstractKeyCode
  case object Code4 extends AbstractKeyCode
  case object Code5 extends AbstractKeyCode
  case object Code6 extends AbstractKeyCode
  case object Code7 extends AbstractKeyCode
  case object Plus extends AbstractKeyCode
  case object Minus extends AbstractKeyCode
  case object Undo extends AbstractKeyCode

}
