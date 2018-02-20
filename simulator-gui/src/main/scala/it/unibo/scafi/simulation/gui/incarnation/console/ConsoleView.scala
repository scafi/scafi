package it.unibo.scafi.simulation.gui.incarnation.console

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.SimulationView

/**
  * simple console out, print node changed
  */
//TODO CREATE A BETTER EXAMPLE
class ConsoleView extends SimulationView{
  override def outNode[N <: World#Node](node: Set[N]): Unit = node foreach {println _}

  override def removeNode[N <: World#ID](node: Set[N]): Unit = node foreach{ x=>println(s"remove: $x")}
  override def outNeighbour[N <: World#Node](nodes: Map[N, Set[N]]): Unit = {}

  /**
    * remove a set of neighbour of a node
    *
    * @tparam ID the id of node
    */
  override def removeNeighbour[ID <: World#ID](nodes: Map[ID, Set[ID]]): Unit = {}

  /**
    * output the device associated to the node
    *
    * @param node the node
    * @tparam N the type of node
    */
  override def outDevice[N <: World#Node](node: Set[N]): Unit = ???

  /**
    * remove all devices associated to a node
    *
    * @param node the node
    * @tparam ID the type of ID
    */
  override def clearDevice[ID <: World#ID](node: Set[ID]): Unit = ???
}