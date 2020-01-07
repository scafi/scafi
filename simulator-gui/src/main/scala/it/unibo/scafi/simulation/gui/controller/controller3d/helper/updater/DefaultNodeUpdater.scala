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

package it.unibo.scafi.simulation.gui.controller.controller3d.helper.updater

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.controller.controller3d.helper.PositionConverter
import it.unibo.scafi.simulation.gui.controller.controller3d.helper.updater.NodeUpdaterHelper._
import it.unibo.scafi.simulation.gui.model.{Network, Node}
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import org.fxyz3d.geometry.MathUtils
import org.scalafx.extras._

/** Class used to update the scene in the view and the simulation, one node at a time, from the simulation updates. */
private[controller3d] class DefaultNodeUpdater(controller: Controller3D, gui3d: NetworkRenderer3D,
                                               simulation: Simulation) extends NodeUpdater {
  private var connectionsInGUI = Map[Int, Set[String]]()
  private var nodesInGUI = Set[Int]()
  private var waitCounterThreshold = -1 //not yet initialized
  private var javaFxWaitCounter = waitCounterThreshold

  gui3d.setActionOnMovedNodes((nodeIDs, movement) => synchronized { //updates simulation when user moves some nodes
    val simulationNodes = nodeIDs.map(nodeID => simulation.network.nodes(nodeID.toInt)).map(node => {
      val nodePosition = node.position
      val vector = PositionConverter.viewToController(movement)
      val result = (node, Option(nodePosition.x + vector._1, nodePosition.y + vector._2, nodePosition.z + vector._3))
      updateNodeInSimulation(simulation, gui3d, result._1, result._2)
      result
    })
    val updatedConnections = simulationNodes
      .map(node => (node._1, node._2, updateNodeConnections(node._1, simulation.network, gui3d)))
    onFX (updatedConnections.foreach(node => { //without runLater it would cause many requests to javaFx
      updateUI(node._2, node._1, UpdateOptions(isPositionNew = true, showMoveDirection = false, node._3._1, node._3._2))
    }))
  })

  /** See [[NodeUpdater.resetNodeCache]] */
  def resetNodeCache(): Unit = synchronized {connectionsInGUI = Map(); nodesInGUI = Set()}

  /** See [[NodeUpdater.updateNode]] */
  def updateNode(nodeId: Int): Unit = synchronized {
    waitForJavaFxIfNeeded() //this waits from time to time for the javaFx to become less congested
    if(nodesInGUI.isEmpty) nodesInGUI = controller.getCreatedNodesID
    val node = simulation.network.nodes(nodeId)
    val newPosition = getNewNodePosition(node, gui3d, simulation)
    val isPositionDifferent = didPositionChange(node, newPosition)
    updateNodeInSimulation(simulation, gui3d, node, newPosition)
    val newAndRemovedConnections = updateNodeConnections(node, simulation.network, gui3d)
    val options = UpdateOptions(isPositionNew = isPositionDifferent, showMoveDirection = true, newAndRemovedConnections)
    updateUI(newPosition, node, options) //using Platform.runLater
  }

  private def updateNodeInSimulation(simulation: Simulation, gui3d: NetworkRenderer3D, node: Node,
                                     newPosition: Option[Product3[Double, Double, Double]]): Unit =
    newPosition.fold(createNodeInSimulation(node, gui3d, simulation))(newPosition =>
      setSimulationNodePosition(node, newPosition, simulation))

  private def updateUI(newPosition: Option[Product3[Double, Double, Double]], node: Node, options: UpdateOptions) {
    val nodeId = node.id.toString
    onFX { //IMPORTANT: without it each node update would cause many requests to the javaFx thread
      createOrMoveNode(newPosition, node, options, gui3d)
      updateNodeText(node, controller.getNodeValueTypeToShow)(gui3d)
      options.newConnections.foreach(otherNodeId => gui3d.connect(nodeId, otherNodeId)) //adding new connections
      options.removedConnections.foreach(otherNodeId => gui3d.disconnect(nodeId, otherNodeId)) //deletes old connections
      updateNodeColor(node, gui3d, controller)
      updateLedActuatorStatus(node, controller, gui3d)
    }
  }

  private def getNewNodePosition(node: Node, gui3d: NetworkRenderer3D,
                                 simulation: Simulation): Option[Product3[Double, Double, Double]] =
    if (nodesInGUI.contains(node.id)) Option(getUpdatedNodePosition(node, gui3d, simulation)) else None

  private def waitForJavaFxIfNeeded(): Unit = {
    val MIN_WAIT_COUNTER = 100
    val MAX_WAIT_COUNTER = 10000
    if(waitCounterThreshold == -1){ //looking at the node count to find out the right value for waitCounterThreshold
      val counterThreshold = 1000000 / Math.pow(Settings.Sim_NumNodes*Settings.Sim_NbrRadius*6.5, 1.4)
      waitCounterThreshold = MathUtils.clamp(counterThreshold, MIN_WAIT_COUNTER, MAX_WAIT_COUNTER).toInt
    }
    javaFxWaitCounter = javaFxWaitCounter - 1
    if(javaFxWaitCounter <= 0) {javaFxWaitCounter = waitCounterThreshold; gui3d.blockUntilThreadIsFree()}
  }

  private def createNodeInSimulation(node: Node, gui3d: NetworkRenderer3D, simulation: Simulation): Unit = {
    gui3d.addNode(node.position, node.id.toString)
    connectionsInGUI += (node.id -> Set())
    nodesInGUI += node.id
    setSimulationNodePosition(node, (node.position.x, node.position.y, node.position.z), simulation)
  }

  private def updateNodeConnections(node: Node, network: Network,
                                    gui3d: NetworkRenderer3D): (Set[String], Set[String]) = { //has side effects
    val connectionsInUI = connectionsInGUI.getOrElse(node.id, Set())
    val connections = network.neighbourhood.getOrElse(node, Set()).map(_.id.toString)
    val newConnections = connections.diff(connectionsInUI)
    val removedConnections = connectionsInUI -- connections
    connectionsInGUI += (node.id -> connections)
    setNewAndRemovedConnections(newConnections, removedConnections, node, gui3d)
    (newConnections, removedConnections)
  }

  private def setNewAndRemovedConnections(newConnections: Set[String], removedConnections: Set[String],
                                          node: Node, gui3d: NetworkRenderer3D): Unit = {
    addOrRemoveNodeFromNeighbours(newConnections, node, adding = true, gui3d)
    addOrRemoveNodeFromNeighbours(removedConnections, node, adding = false, gui3d)
  }

  private def addOrRemoveNodeFromNeighbours(connections: Set[String], node: Node, adding: Boolean,
                                            gui3d: NetworkRenderer3D): Unit = {
    val nodeId = node.id.toString
    connections.foreach(neighbourId => {
      val neighbourIntId = neighbourId.toInt
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
