package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.controller.{Command, InputCommandSingleton}
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
import it.unibo.scafi.simulation.gui.view.scalaFX.AbstractFXSimulationPane

/*
TODO CREATE A GENERIC CONCEPT
 */
import scalafx.scene.input.{KeyCode, KeyEvent}
trait KeyboardManager [ID <: World#ID] extends AbstractKeyboardManager[ID]{

  self : AbstractFXSimulationPane with FXSelectionArea[ID] =>
  override type KEYCODE = KeyCode
  implicit val inputController : InputCommandSingleton
  abstractToReal += Code1 -> KeyCode.Digit1
  abstractToReal += Code2 -> KeyCode.Digit2
  abstractToReal += Code3 -> KeyCode.Digit3
  abstractToReal += Code4 -> KeyCode.Digit4
  import scalafx.Includes._
  self.onKeyPressed = (e : KeyEvent) => {
    e.consume()
    commands.filter{x => {abstractToReal(x._1) == e.getCode}} foreach {x =>
      inputController.instance().get.exec(x._2(self.selected))
    }
  }
}
