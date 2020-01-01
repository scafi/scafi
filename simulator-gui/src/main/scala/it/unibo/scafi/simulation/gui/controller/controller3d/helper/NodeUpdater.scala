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

package it.unibo.scafi.simulation.gui.controller.controller3d.helper

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.controller.controller3d.helper.NodeUpdaterHelper._
import it.unibo.scafi.simulation.gui.model.{Network, Node}
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import org.fxyz3d.geometry.MathUtils
import scalafx.application.Platform

/** Class used to update the scene in the view and the simulation, one node at a time, from the simulation updates. */
private[controller3d] class NodeUpdater(controller: Controller3D, gui3d: NetworkRenderer3D, simulation: Simulation) {
  private var connectionsInGUI = Map[Int, Set[String]]()
  private var nodesInGUI = Set[Int]()
  private var waitCounterThreshold = -1 //not yet initialized
  private var javaFxWaitCounter = waitCounterThreshold

  gui3d.setActionOnMovedNodes(synchronized {_.foreach(node => //updating simulation when user moves the selected nodes
    setSimulationNodePosition(simulation.network.nodes(node._1.toInt), node._2, simulation))})

  /** Resets the collections that keep information about the nodes. */
  def resetNodeCache(): Unit = synchronized {connectionsInGUI = Map(); nodesInGUI = Set()}

  /** Most important method of this class. It updates the specified node in the UI and in the simulation.
   * Most of the calculations are done outside of theJavaFx thread to reduce lag.
   * @param nodeId the id of the node to update */
  def updateNode(nodeId: Int): Unit = synchronized { //FIXME: when manually moving nodes the simulation and view disagree on nodes position
    waitForJavaFxIfNeeded() //this waits from time to time for the javaFx to become less congested
    if(nodesInGUI.isEmpty) nodesInGUI = controller.getCreatedNodesID
    val node = simulation.network.nodes(nodeId)
    val newPosition = getNewNodePosition(node, gui3d, simulation)
    val isPositionDifferent = didPositionChange(node, newPosition)
    updateNodeInSimulation(simulation, gui3d, node, newPosition)
    val newAndRemovedConnections = updateNodeConnections(node, simulation.network, gui3d)
    updateUI(newPosition, node, isPositionDifferent, newAndRemovedConnections) //using Platform.runLater
  }

  private def updateNodeInSimulation(simulation: Simulation, gui3d: NetworkRenderer3D, node: Node,
                                     newPosition: Option[Product3[Double, Double, Double]]): Unit = {
    newPosition.fold(createNodeInSimulation(node, gui3d, simulation))(newPosition =>
      setSimulationNodePosition(node, newPosition, simulation))
  }

  private def updateUI(newPosition: Option[Product3[Double, Double, Double]], node: Node, isPositionDifferent: Boolean,
                       connections: (Set[String], Set[String])): Unit = {
    val nodeId = node.id.toString
    Platform.runLater { //IMPORTANT: without it each node update would cause many requests to the javaFx thread
      createOrMoveNode(newPosition, node, isPositionDifferent, gui3d)
      updateNodeText(node, controller.getNodeValueTypeToShow)(gui3d)
      connections._1.foreach(otherNodeId => gui3d.connect(nodeId, otherNodeId)) //adding new connections
      connections._2.foreach(otherNodeId => gui3d.disconnect(nodeId, otherNodeId)) //deleting removed connections
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
                                    gui3d: NetworkRenderer3D): (Set[String], Set[String]) = {
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
}

object NodeUpdater {
  def apply(controller: Controller3D, gui3d: NetworkRenderer3D, simulation: Simulation): NodeUpdater =
    new NodeUpdater(controller, gui3d, simulation)
}
