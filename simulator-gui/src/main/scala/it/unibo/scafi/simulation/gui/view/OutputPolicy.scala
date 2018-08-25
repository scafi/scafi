package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.core.World

/**
  * a strategy to render a node
  */
trait OutputPolicy {
  /**
    * the type of node to draw
    */
  type OUTPUT_NODE
  type NODE = World#Node
  type DEVICE = World#Device
  /**
    * take a world node and create its graphics representation
    * @param node the input node
    * @return the graphics node created
    */
  def nodeGraphicsNode(node : World#Node) : OUTPUT_NODE

  /**
    * create a graphics representation of device passed
    * @param node the node
    * @param dev the device attached on node
    * @return the graphics representation
    */
  def deviceToGraphicsNode(node: OUTPUT_NODE, dev : DEVICE) : Option[OUTPUT_NODE]

  /**
    * update the device value
    * @param dev the device
    * @param graphicsDevice graphics device representation
    */
  def updateDevice(node : OUTPUT_NODE, dev: DEVICE, graphicsDevice : Option[OUTPUT_NODE])
}
