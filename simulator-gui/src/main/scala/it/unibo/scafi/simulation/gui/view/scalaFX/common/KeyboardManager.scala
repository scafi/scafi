package it.unibo.scafi.simulation.gui.view.scalaFX.common

import it.unibo.scafi.simulation.gui.controller.InputCommandController
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.ZoomablePane

import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.Pane
trait KeyboardManager extends AbstractKeyboardManager {

  self : AbstractFXSimulationPane with FXSelectionArea =>
  override type KEYCODE = KeyCode
  implicit val inputController : InputCommandController[_]
  abstractToReal += Code1 -> KeyCode.Digit1
  abstractToReal += Code2 -> KeyCode.Digit2
  abstractToReal += Code3 -> KeyCode.Digit3
  abstractToReal += Code4 -> KeyCode.Digit4
  import scalafx.Includes._

  self.onKeyPressed = (e : KeyEvent) => {
    e.consume()
    commands.find{x => {abstractToReal(x._1) == e.getCode}} foreach {x =>
      inputController.exec(x._2(self.selected))
    }
  }
}
