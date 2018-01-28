package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.controller.{Command, InputCommandController}
import it.unibo.scafi.simulation.gui.model.core.World

class ScafiInputController(w: ScafiLikeWorld) extends InputCommandController[ScafiLikeWorld] {
  case class MoveCommand(ids : Map[ScafiLikeWorld#ID, ScafiLikeWorld#P]) extends Command {
    //TODO UNMAKE
    override def make(): Unit = w.moveNodes(ids)

    override def unmake(): Unit = ???
  }

  def moveNodes(ids : Map[Int, w.P]) =  this.commands = MoveCommand(ids) :: this.commands

}