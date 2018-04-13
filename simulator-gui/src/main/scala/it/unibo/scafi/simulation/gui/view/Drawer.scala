package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.World

/**
  * a drawer of node
  */
trait Drawer {
  /**
    * the type of node to draw
    */
  type OUTPUTNODE

  /**
    * take a world node and create its graphics representation
    * @param node the input node
    * @tparam INPUTNODE a generic input node
    * @return the graphics node created
    */
  def nodeGraphicsNode[INPUTNODE <: World#NODE] (node : INPUTNODE) : OUTPUTNODE

  def deviceToGraphicsNode[INPUTDEV <: World#DEVICE](node: OUTPUTNODE,dev : INPUTDEV, lastValue : Option[OUTPUTNODE]) : Option[OUTPUTNODE]
}
