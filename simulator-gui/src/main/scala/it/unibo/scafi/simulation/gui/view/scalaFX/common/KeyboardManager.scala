package it.unibo.scafi.simulation.gui.view.scalaFX.common

import it.unibo.scafi.simulation.gui.controller.input.inputCommandController
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._

import scalafx.scene.input.{KeyCode, KeyEvent}
trait KeyboardManager extends AbstractKeyboardManager {

  self: AbstractFXSimulationPane with FXSelectionArea =>
  override type KEYCODE = KeyCode
  abstractToReal += Code1 -> KeyCode.Digit1
  abstractToReal += Code2 -> KeyCode.Digit2
  abstractToReal += Code3 -> KeyCode.Digit3
  abstractToReal += Code4 -> KeyCode.Digit4
  abstractToReal += Code5 -> KeyCode.Digit5

  import scalafx.Includes._

  self.onKeyPressed = (e: KeyEvent) => {
    e.consume()
    idsValueCommands.find { x => {
      abstractToReal(x._1) == e.getCode
    }
    } foreach { x =>
      val arg = x._2(valueMapped(x._1),selected)
      factoryMapped(x._1).create(arg) foreach {inputCommandController.exec(_)}
    }
    genericCommands.find { x => {
      abstractToReal(x._1) == e.getCode
    }
    } foreach { x =>
      val arg = x._2(valueMapped(x._1))
      factoryMapped(x._1).create(arg) foreach {inputCommandController.exec(_)}
    }
    idsCommands.find { x => {
      abstractToReal(x._1) == e.getCode
    }
    } foreach { x =>
      val arg = x._2(selected)
      factoryMapped(x._1).create(arg) foreach {inputCommandController.exec(_)}
    }
  }
}

