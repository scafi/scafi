package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import javafx.event.EventHandler

import it.unibo.scafi.simulation.gui.controller.{Command, SimpleInputController}
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.view.scalaFX.AbstractFXSimulationPane

import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
//TODO SOLVE THE PROBLE
trait KeyboardManager {
  self : AbstractFXSimulationPane with SelectionArea =>

  implicit val inputController : SimpleInputController[ScafiLikeWorld]
  private var commands : Map[KeyCode,Set[Int] => Command] = Map.empty
  def addCommand(code : KeyCode, command : Set[Int] => Command) = commands += code -> command
  import scalafx.Includes._
  self.onKeyPressed = (e : KeyEvent) => {
    e.consume()
    commands.filter{x => {x._1 == e.getCode}} foreach {x => inputController.exec(x._2(self.selected.asInstanceOf[Set[Int]]))}
  }
  /**
    *
  this.onKeyTyped = new EventHandler[KeyEvent] {
    override def handle(event: KeyEvent): Unit = {
      println("here")

    }
  }
    */
}
