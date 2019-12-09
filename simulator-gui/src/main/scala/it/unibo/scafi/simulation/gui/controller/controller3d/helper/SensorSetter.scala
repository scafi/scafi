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
import it.unibo.scafi.simulation.gui.Simulation
import it.unibo.scafi.simulation.gui.model.Node
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum

private[controller3d] class SensorSetter(simulationPanel: NetworkRenderingPanel, simulation: Simulation) {

  def setSensor(sensorName: String, value: Any, selectionAttempted: Boolean): Unit = { //TODO: don't copy-paste
    val selectedNodes = getSelectedNodes(simulationPanel.getSelectedNodesIDs)
    setNodesSensor(selectedNodes, sensorName, value)
    simulation.setSensor(sensorName, value.toString.toBoolean, selectedNodes)
    if (selectedNodes.isEmpty && !selectionAttempted) {
      setNodesSensor(simulation.network.nodes.values, sensorName, value)
      simulation.setSensor(sensorName, value)
    }
  }

  private def setNodesSensor(nodes: Iterable[Node], sensorName: String, value: Any): Unit =
    nodes.foreach(_.setSensor(sensorName, value))

  def handleNumberButtonPress(sensorIndex: Int): Unit = {
    getSensorName(sensorIndex).foreach(sensorName => {
      val selectedNodes = getSelectedNodes(simulationPanel.getSelectedNodesIDs)
      simulationPanel.getInitialSelectedNodeId.foreach(initialNodeId => {
        val initialNode = selectedNodes.filter(_.id == initialNodeId.toInt).head
        val sensorValue = initialNode.getSensorValue(sensorName)
        val newSensorValue = sensorValue match {case value: Boolean => !value}
        selectedNodes.foreach(node => setNodeSensor(node, sensorName, newSensorValue))
        simulation.setSensor(sensorName, newSensorValue, selectedNodes)
      })
    })
  }

  private def getSelectedNodes(selectedNodeIDs: Set[String]): Set[Node] = {
    if(simulation.network == null){
      Set()
    } else {
      simulation.network.nodes.filter(node => selectedNodeIDs.contains(node._2.id.toString)).values.toSet
    }
  }

  private def getSensorName(sensorIndex: Int): Option[String] = SensorEnum.fromInt(sensorIndex).map(_.name)

  private def setNodeSensor(node: Node, sensorName: String, newSensorValue: Boolean): Unit = {
    val selectedNode = simulation.network.nodes(node.id)
    selectedNode.setSensor(sensorName, newSensorValue)
    NodeUpdater.updateNodeColorBySensors(node, simulationPanel)
  }
}

private[controller3d] object SensorSetter {
  def apply(simulationPanel: NetworkRenderingPanel, simulation: Simulation): SensorSetter =
    new SensorSetter(simulationPanel, simulation)
}
