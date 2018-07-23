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
  type NODE = World#Node
  type DEVICE = World#Device
  /**
    * take a world node and create its graphics representation
    * @param node the input node
    * @return the graphics node created
    */
  def nodeGraphicsNode(node : World#Node) : OUTPUTNODE

  /**
    * create a graphics representation of device passed
    * @param node the node
    * @param dev the device attached on node
    * @return the graphics representation
    */
  def deviceToGraphicsNode(node: OUTPUTNODE,dev : DEVICE) : Option[OUTPUTNODE]

  /**
    * update the device value
    * @param dev the device
    * @param graphicsDevice graphics device representation
    */
  def updateDevice(node : OUTPUTNODE, dev: DEVICE, graphicsDevice : Option[OUTPUTNODE])
}
