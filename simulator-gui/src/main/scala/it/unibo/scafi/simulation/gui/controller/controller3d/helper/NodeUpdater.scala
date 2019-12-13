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
  private val MAX_WAIT_COUNTER = 10000
  private var javaFxWaitCounter = MAX_WAIT_COUNTER

  def updateNode(nodeId: Int, gui: SimulatorUI3D, simulation: Simulation, controller: Controller3D): Unit = {
    waitForJavaFxIfNeeded(gui) //this waits from time to time that the javafX thred is not too congested
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

  private def waitForJavaFxIfNeeded(gui: SimulatorUI3D): Unit = {
    javaFxWaitCounter = javaFxWaitCounter - 1;
    if(javaFxWaitCounter <= 0){
      javaFxWaitCounter = MAX_WAIT_COUNTER
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

  private def updateNodeConnections(node: Node, network: Network, gui3d: NetworkRenderingPanel): Unit = {
    val connectionsInUI = connectionsInGUI.getOrElse(node.id, Set())
    val connections = network.neighbourhood.getOrElse(node, Set()).map(_.id.toString)
    val newConnections = connections.diff(connectionsInUI)
    val removedConnections = connectionsInUI.diff(connections)
    val nodeId = node.id.toString
    connectionsInGUI += (node.id -> connections)
    setNewConnections(newConnections, nodeId, gui3d)
    removedConnections.foreach(connection => gui3d.disconnect(nodeId, connection))
  }

  private def setNewConnections(newConnections: Set[String], nodeId: String, gui3d: NetworkRenderingPanel): Unit = {
    newConnections.foreach(neighbourId => {
      gui3d.connect(nodeId, neighbourId)
      val neighbourIntId = neighbourId.toInt
      val previousNodeNeighbours = connectionsInGUI.getOrElse(neighbourIntId, Set())
      connectionsInGUI += (neighbourIntId -> (previousNodeNeighbours + nodeId))
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
