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
import it.unibo.scafi.simulation.gui.controller.ControllerUtils._
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.model.{Network, Node, NodeValue}
import it.unibo.scafi.simulation.gui.view.ui3d.SimulatorUI3D
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import it.unibo.scafi.space.Point3D

import scala.util.Try

private[controller3d] object NodeUpdater {

  def updateNode(nodeId: Int, gui: SimulatorUI3D, simulation: Simulation, controller: Controller3D): Unit = {
    val gui3d = gui.getSimulationPanel
    val node = simulation.network.nodes(nodeId)
    createOrMoveNode(node, gui3d, simulation)
    updateNodeText(node, controller.getNodeValueTypeToShow)(gui3d)
    updateNodeConnections(gui3d, node, simulation.network)
    if(controller.getObservation()(node.export)){
      gui3d.setNodeColor(node.id.toString, Settings.Color_observation)
    } else if(controller.isObservationSet) {
      updateNodeColorBySensors(node, gui3d)
    }
  }

  def updateNodeColorBySensors(node: Node, simulationPanel: NetworkRenderingPanel): Unit = {
    val firstEnabledSensorInNode = node.sensors.filter(_._2.equals(true)).keys.headOption
    val sensorColor = firstEnabledSensorInNode.map(SensorEnum.getColor(_).getOrElse(Settings.Color_device))
    simulationPanel.setNodeColor(node.id.toString, sensorColor.getOrElse(Settings.Color_device))
  }

  /**
   * Returns wether the node was created or not
   * */
  private def createOrMoveNode(node: Node, gui3d: NetworkRenderingPanel, simulation: Simulation): Unit = {
    val nodeId = node.id.toString
    val nodePositionInUI = gui3d.getNodePosition(nodeId)
    if (nodePositionInUI.isEmpty) {
      gui3d.addNode(node.position, nodeId) //creating the node in ui if not already present
    } else {
      moveNode(node, gui3d, simulation) //updating node position if the node moved
    }
  }

  private def updateNodeText(node: Node, valueToShow: NodeValue)(implicit gui3d: NetworkRenderingPanel): Unit = {
    val outputString = Try(Settings.To_String(node.export))
    if(outputString.isSuccess && !outputString.get.equals("")) {
      valueToShow match {
        case NodeValue.ID => setNodeText(node, node.id.toString)
        case NodeValue.EXPORT => setNodeText(node, formatExport(node.export))
        case NodeValue.POSITION => setNodeText(node, formatPosition(node.position))
        case NodeValue.POSITION_IN_GUI => gui3d.setNodeTextAsUIPosition(node.id.toString, formatProductPosition)
        case NodeValue.SENSOR(name) => setNodeText(node, node.getSensorValue(name).toString)
        case _ => setNodeText(node, "")
      }
    }
  }

  private def setNodeText(node: Node, text: String)(implicit gui3d: NetworkRenderingPanel): Boolean =
    gui3d.setNodeText(node.id.toString, text) //updating the value of the node's label

  private def updateNodeConnections(gui3d: NetworkRenderingPanel, node: Node, network: Network): Unit = {
    val nodeId = node.id.toString
    val connectionsInUI = gui3d.getNodesConnectedToNode(nodeId).getOrElse(Set())
    val connections = network.neighbourhood.getOrElse(node, Set()).map(_.id.toString)
    val newConnections = connections.diff(connectionsInUI)
    val removedConnections = connectionsInUI.diff(connections)
    newConnections.foreach(gui3d.connect(nodeId, _))
    removedConnections.foreach(connection => gui3d.disconnect(nodeId, connection))
  }

  //the problem is that in neighboursMap the removed connections never get removed
  private def moveNode(node: Node, gui3d: NetworkRenderingPanel, simulation: Simulation): Unit = { //TODO: connections should be removed correctly
    val vector = Try(Settings.Movement_Activator_3D(node.export)).getOrElse((0.0, 0.0, 0.0))
    if(vector != (0, 0, 0)) {
      val currentPosition = node.position
      val newPosition = (currentPosition.x + vector._1, currentPosition.y + vector._2, currentPosition.z + vector._3)
      gui3d.moveNode(node.id.toString, newPosition)
      simulation.setPosition(node)
      node.position = new Point3D(newPosition._1, newPosition._2, newPosition._3)
    }
  }

}
