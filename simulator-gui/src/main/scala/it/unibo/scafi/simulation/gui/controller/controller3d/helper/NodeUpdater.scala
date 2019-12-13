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

import it.unibo.scafi.renderer3d.manager.NetworkRenderingPanel
import it.unibo.scafi.renderer3d.util.RichScalaFx.RichMath
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.controller.controller3d.helper.NodeUpdaterHelper._
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.model.{Network, Node}
import it.unibo.scafi.simulation.gui.view.ui3d.SimulatorUI3D
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import scalafx.application.Platform

private[controller3d] object NodeUpdater {

  private var connectionsInGUI = Map[Int, Set[String]]()
  private var nodesInGUI = Set[Int]()
  private val MIN_WAIT_COUNTER = 100
  private val MAX_WAIT_COUNTER = 10000
  private var waitCounterThreshold = -1 //not yet initialized
  private var javaFxWaitCounter = waitCounterThreshold

  def updateNode(nodeId: Int, gui: SimulatorUI3D, simulation: Simulation, controller: Controller3D): Unit = {
    waitForJavaFxIfNeeded(gui, controller) //this waits from time to time for the javaFx to become less congested
    Platform.runLater { //IMPORTANT: without it each node update would cause many requests to the javaFx thread
      if(nodesInGUI.isEmpty) nodesInGUI = controller.getCreatedNodesID
      val gui3d = gui.getSimulationPanel
      val node = simulation.network.nodes(nodeId)
      createOrMoveNode(node, simulation, controller, gui3d)
      updateNodeText(node, controller.getNodeValueTypeToShow)(gui3d)
      updateNodeConnections(node, simulation.network, gui3d)
      updateNodeColor(node, controller, gui3d)
      updateLedActuatorRadius(node, controller, gui3d)
    }
  }

  private def waitForJavaFxIfNeeded(gui: SimulatorUI3D, controller: Controller3D): Unit = {
    if(waitCounterThreshold == -1){ //looking at the node count to find out the right value for waitCounterThreshold
      val counterThreshold = 1000000 / Math.pow(controller.getCreatedNodesID.size, 1.3)
      waitCounterThreshold = RichMath.clamp(counterThreshold, MIN_WAIT_COUNTER, MAX_WAIT_COUNTER).toInt
    }
    javaFxWaitCounter = javaFxWaitCounter - 1;
    if(javaFxWaitCounter <= 0){
      javaFxWaitCounter = waitCounterThreshold
      gui.getSimulationPanel.blockUntilThreadIsFree()
    }
  }

  def updateNodeColorBySensors(node: Node, simulationPanel: NetworkRenderingPanel): Unit = {
    val firstEnabledSensorInNode = node.sensors.filter(_._2.equals(true)).keys.headOption
    val sensorColor = firstEnabledSensorInNode.map(SensorEnum.getColor(_).getOrElse(Settings.Color_device))
    simulationPanel.setNodeColor(node.id.toString, sensorColor.getOrElse(Settings.Color_device))
  }

  private def createOrMoveNode(node: Node, simulation: Simulation, controller: Controller3D,
                               gui3d: NetworkRenderingPanel): Unit = {
    if (nodesInGUI.contains(node.id)) {
      updateNodePosition(node, gui3d, simulation)
    } else {
      createNode(node, gui3d, simulation) //creating the node in ui if not already present
    }
  }

  private def createNode(node: Node, gui3d: NetworkRenderingPanel, simulation: Simulation): Unit = {
    gui3d.addNode(node.position, node.id.toString)
    connectionsInGUI += (node.id -> Set())
    nodesInGUI += node.id
    setSimulationNodePosition(node, (node.position.x, node.position.y, node.position.z), simulation)
  }

  private def updateNodeConnections(node: Node, network: Network, gui3d: NetworkRenderingPanel): Unit = { //TODO
    val connectionsInUI = connectionsInGUI.getOrElse(node.id, Set())
    val connections = network.neighbourhood.getOrElse(node, Set()).map(_.id.toString)
    val newConnections = connections.diff(connectionsInUI)
    val removedConnections = connectionsInUI.diff(connections)
    connectionsInGUI += (node.id -> connections)
    setNewAndRemovedConnections(newConnections, removedConnections, node, gui3d)
  }

  private def setNewAndRemovedConnections(newConnections: Set[String], removedConnections: Set[String],
                                          node: Node, gui3d: NetworkRenderingPanel): Unit = {
    addOrRemoveNodeFromNeighbours(newConnections, node, adding = true, gui3d)
    addOrRemoveNodeFromNeighbours(removedConnections, node, adding = false, gui3d)
  }

  private def addOrRemoveNodeFromNeighbours(connections: Set[String], node: Node, adding: Boolean,
                                            gui3d: NetworkRenderingPanel): Unit = {
    val nodeId = node.id.toString
    connections.foreach(neighbourId => {
      val neighbourIntId = neighbourId.toInt
      val previousNodeNeighbours = connectionsInGUI.getOrElse(neighbourIntId, Set())
      val updatedNodeNeighbours = if(adding) previousNodeNeighbours + nodeId else previousNodeNeighbours - nodeId
      connectionsInGUI += (neighbourIntId -> updatedNodeNeighbours)
      if(adding) gui3d.connect(nodeId, neighbourId) else gui3d.disconnect(nodeId, neighbourId)
    })
  }

  private def updateNodeColor(node: Node, controller: Controller3D, gui3d: NetworkRenderingPanel): Unit = {
    if(controller.getObservation()(node.export)){
      gui3d.setNodeColor(node.id.toString, Settings.Color_observation)
    } else if(controller.isObservationSet) {
      updateNodeColorBySensors(node, gui3d)
    }
  }

}
