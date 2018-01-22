package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.model.common.world.ObservableWorld

/**
  * define a generic controller that controls input by a generic view
  */
trait InputLikeController[W <: ObservableWorld] extends Controller[W]{
  /**
    * has a list of command reiceved
    * @return
    */
  def commands : List[Command]

  /**
    * put the command in the queue list
    * @param c
    */
  def exec(c : Command)

  override def onTick(float: Float): Unit = commands foreach {_ make()}
}

trait Command {
  def make() : Unit

  def unmake() : Unit
}
