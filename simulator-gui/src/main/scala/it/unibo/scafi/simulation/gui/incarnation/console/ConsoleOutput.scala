package it.unibo.scafi.simulation.gui.incarnation.console

import it.unibo.scafi.simulation.gui.model.core.Node
import it.unibo.scafi.simulation.gui.view.SimulationOutput

/**
  * simple console out, print node changed
  */
class ConsoleOutput extends SimulationOutput{
  override def out(node: Node): Unit = println(node)

  override def out(node: Set[Node]): Unit = node foreach {println(_)}
}
