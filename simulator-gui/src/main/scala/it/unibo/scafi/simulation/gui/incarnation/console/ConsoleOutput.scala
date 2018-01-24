package it.unibo.scafi.simulation.gui.incarnation.console

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.SimulationOutput

/**
  * simple console out, print node changed
  */
class ConsoleOutput extends SimulationOutput{
  override def out[N <: World#Node](node: Set[N]): Unit = node foreach {println _}

  override def remove[N <: World#ID](node: Set[N]): Unit = node foreach{ x=>println(s"remove: $x")}

  override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = {
    print(s"$node -> ")
    neighbour map {_.id} foreach {print _}
    println
  }
}