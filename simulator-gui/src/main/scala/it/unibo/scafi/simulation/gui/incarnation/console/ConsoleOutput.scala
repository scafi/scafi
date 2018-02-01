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

  /**
    * out a neighbour of a node
    *
    * @tparam N the type of node
    */
  override def outNeighbour[N <: World#Node](nodes: Map[N, Set[N]]): Unit = {}

  /**
    * remove a set of neighbour of a node
    *
    * @tparam ID the id of node
    */
  override def removeNeighbour[ID <: World#ID](nodes: Map[ID, Set[ID]]): Unit = {}
}