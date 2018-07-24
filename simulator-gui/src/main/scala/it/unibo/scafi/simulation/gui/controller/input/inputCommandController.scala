package it.unibo.scafi.simulation.gui.controller.input

/**
  * an input controller that execute command
  */
object inputCommandController extends InputController {
  protected var commands : List[Command] = List.empty
  /**
    * put the command in the queue list
    * @param c
    */
  final def exec(c : Command) = commands = c :: commands

  override def onTick(float: Float): Unit = {
    commands foreach {_ make()}
    commands = List.empty
  }
}

