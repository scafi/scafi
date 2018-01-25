package it.unibo.scafi.simulation.gui.incarnation.console

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.SimulationOutput

/**
  * simple console out, print node changed
  */
//TODO CREATE A BETTER EXAMPLE
class ConsoleOutput extends SimulationOutput{
  override def outNode[N <: World#Node](node: Set[N]): Unit = node foreach {println _}

  override def removeNode[N <: World#ID](node: Set[N]): Unit = node foreach{ x=>println(s"remove: $x")}

  override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = {
    print(s"$node -> ")
    neighbour map {_.id} foreach {print _}
    println
  }

  override def removeNeighbour[ID <: World#ID](node: ID, neighbour: Set[ID]): Unit = {}

  /**
    * output the device associated to the node
    *
    * @param node the node
    * @tparam N the type of node
    */
  override def outDevice[N <: World#Node](node: N): Unit = {}

  /**
    * remove all devices associated to a node
    *
    * @param node the node
    * @tparam N the type of node
    */
  override def clearDevice[N <: World#ID](node: N): Unit = {}
}