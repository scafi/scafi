/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.gui.Simulation
import it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater.NodeUpdaterHelper._
import it.unibo.scafi.simulation.gui.model.{Network, Node}
import org.scalafx.extras._

/** Class used to update the scene in the view and the simulation, one node at a time, from the simulation updates. */
private[controller3d] class DefaultNodeUpdater(controller: Controller3D, gui3d: NetworkRenderer3D,
                                               simulation: Simulation) extends NodeUpdater {

  private type ID = Int
  private val javaFxWaiter = JavaFxWaiter(gui3d)
  private var connectionsInGUI = Map[Int, Set[ID]]()
  private var nodesInGUI = Set[Int]()
  private var movingNodes = Set[Int]()

  gui3d.setActionOnMovedNodes((nodeIDs, movement) => synchronized { //updates simulation when user moves some nodes
    val nodesAndConnections = getMovedNodes(nodeIDs, movement, simulation)
      .map(node => (node._1, node._2, updateNodeConnections(node._1, simulation.network, gui3d)))
    nodesAndConnections.foreach(node => updateNodeInSimulation(simulation, gui3d, node._1, node._2))
    onFX (nodesAndConnections.foreach(node => { //without runLater it would cause many requests to javaFx
      updateUI(node._2, updateLedStatus = false, node._1, UpdateOptions(isPositionNew = true, showMoveDirection = false,
        stoppedMoving = false, node._3._1, node._3._2, None, None))
    }))
  })

  /** See [[NodeUpdater.resetNodeCache]] */
  def resetNodeCache(): Unit = synchronized {connectionsInGUI = Map(); nodesInGUI = Set()}

  /** See [[NodeUpdater.updateNode]] */
  def updateNode(nodeId: Int): Unit = synchronized {
    javaFxWaiter.waitForJavaFxIfNeeded() //this waits from time to time for the javaFx to become less congested
    if(nodesInGUI.isEmpty) nodesInGUI = controller.getCreatedNodesID
    val node = simulation.network.nodes(nodeId)
    val newPosition = getNewNodePosition(node, gui3d, simulation)
    val isPositionDifferent = didPositionChange(node, newPosition)
    updateNodeInSimulation(simulation, gui3d, node, newPosition)
    val options = getUIUpdateOptions(node, newPosition, isPositionDifferent)
    if(isPositionDifferent) movingNodes += nodeId else movingNodes -= nodeId
    updateUI(newPosition, updateLedStatus = true, node, options) //using Platform.runLater
  }

  private def getUIUpdateOptions(node: Node, newPosition: Option[Product3[Double, Double, Double]],
                                 isPositionDifferent: Boolean) = {
    val nodeStoppedMoving = !isPositionDifferent && movingNodes.contains(node.id)
    val (newConnections, oldConnections) = updateNodeConnections(node, simulation.network, gui3d)
    UpdateOptions(isPositionNew = isPositionDifferent, showMoveDirection = true, stoppedMoving = nodeStoppedMoving,
      newConnections, oldConnections,  getUpdatedNodeColor(node, controller), Option(controller.getNodeValueTypeToShow))
  }

  private def updateNodeInSimulation(simulation: Simulation, gui3d: NetworkRenderer3D, node: Node,
                                     newPosition: Option[Product3[Double, Double, Double]]): Unit =
    newPosition.fold(createNodeInSimulation(node, gui3d, simulation))(newPosition =>
      setSimulationNodePosition(node, newPosition, simulation))

  private def updateUI(newPosition: Option[Product3[Double, Double, Double]], updateLedStatus: Boolean, node: Node,
                       options: UpdateOptions) { //if updateLedStatus == false it forces the led status to false
    val nodeId = node.id
    onFX { //IMPORTANT: without it each node update would cause many requests to the javaFx thread
      createOrMoveNode(newPosition, node, options, gui3d)
      options.valueType.fold()(valueType => updateNodeText(node, valueType)(gui3d))
      options.newConnections.foreach(otherNodeId => gui3d.connect(nodeId, otherNodeId)) //adding new connections
      options.removedConnections.foreach(otherNodeId => gui3d.disconnect(nodeId, otherNodeId)) //deletes old connections
      options.color.fold()(color => gui3d.setNodeColor(nodeId, color))
      if(updateLedStatus) updateLedActuatorStatus(node, controller, gui3d, options.isPositionNew)
    }
  }

  private def getNewNodePosition(node: Node, gui3d: NetworkRenderer3D,
                                 simulation: Simulation): Option[Product3[Double, Double, Double]] =
    if (nodesInGUI.contains(node.id)) Option(getUpdatedNodePosition(node, gui3d, simulation)) else None

  private def createNodeInSimulation(node: Node, gui3d: NetworkRenderer3D, simulation: Simulation): Unit = {
    connectionsInGUI += (node.id -> Set())
    nodesInGUI += node.id
    setSimulationNodePosition(node, (node.position.x, node.position.y, node.position.z), simulation)
  }

  private def updateNodeConnections(node: Node, network: Network,
                                    gui3d: NetworkRenderer3D): (Set[ID], Set[ID]) = { //has side effects
    val connectionsInUI = connectionsInGUI.getOrElse(node.id, Set())
    val connections = network.neighbourhood.getOrElse(node, Set()).map(_.id)
    val newConnections = connections.diff(connectionsInUI)
    val removedConnections = connectionsInUI -- connections
    connectionsInGUI += (node.id -> connections)
    setNewAndRemovedConnections(newConnections, removedConnections, node, gui3d)
    (newConnections, removedConnections)
  }

  private def setNewAndRemovedConnections(newConnections: Set[ID], removedConnections: Set[ID],
                                          node: Node, gui3d: NetworkRenderer3D): Unit = {
    addOrRemoveNodeFromNeighbours(newConnections, node, adding = true, gui3d)
    addOrRemoveNodeFromNeighbours(removedConnections, node, adding = false, gui3d)
  }

  private def addOrRemoveNodeFromNeighbours(connections: Set[ID], node: Node, adding: Boolean,
                                            gui3d: NetworkRenderer3D): Unit = {
    val nodeId = node.id
    connections.foreach(neighbourId => {
      val neighbourIntId = neighbourId
      val previousNodeNeighbours = connectionsInGUI.getOrElse(neighbourIntId, Set())
      val updatedNodeNeighbours = if(adding) previousNodeNeighbours + nodeId else previousNodeNeighbours - nodeId
      connectionsInGUI += (neighbourIntId -> updatedNodeNeighbours)
    })
  }

  /** See [[NodeUpdater.updateNodeColorBySensors]] */
  def updateNodeColorBySensors(node: Node, gui3d: NetworkRenderer3D): Unit =
    NodeUpdaterHelper.updateNodeColorBySensors(node, gui3d)
}

object DefaultNodeUpdater {
  def apply(controller: Controller3D, gui3d: NetworkRenderer3D, simulation: Simulation): DefaultNodeUpdater =
    new DefaultNodeUpdater(controller, gui3d, simulation)
}
