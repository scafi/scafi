package it.unibo.scafi.simulation.gui.incarnation.console

import it.unibo.scafi.simulation.gui.model.core.Node
import it.unibo.scafi.simulation.gui.view.SimulationOutput

/**
  * simple console out, print node changed
  */
class ConsoleOutput extends SimulationOutput{
  override def out(node: Set[Node]): Unit = node foreach {println _}
  override def remove(node: Set[Node]): Unit = node foreach{ x=>println(s"remove: $x")}
  override def outNeighbour(node: Node, neighbour: Set[Node]): Unit ={
    print(s"$node -> ")
    neighbour map {_.id} foreach {print _}
    println
  }
}
