package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld

/**
  * define a generic controller that controls input by a generic view
  */
trait InputCommandController[W <: AggregateWorld] extends InputController[W]{
  protected var commands : List[Command] = List.empty
  /**
    * put the command in the queue list
    * @param c
    */
  def exec(c : Command) = commands = c :: commands

  override def onTick(float: Float): Unit = {
    commands foreach {_ make()}
    commands = List.empty
  }
}

trait Command {
  def make() : Unit

  def unmake() : Unit
}

