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

package it.unibo.scafi.simulation.frontend.controller.controller3d.helper.sensor

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.frontend.Simulation
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater.NodeUpdater
import it.unibo.scafi.simulation.frontend.model.Node
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum

/**
 * Helper class to set and update the node's sensors.
 * */
private[controller3d] class DefaultSensorSetter(simulationPanel: NetworkRenderer3D, simulation: Simulation,
                                                nodeUpdater: NodeUpdater) extends SensorSetter {

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.helper.sensor.SensorSetter#setSensor(java.lang.String, java.lang.Object, boolean)]] */
  def setSensor(sensorName: String, value: Any, selectionAttempted: Boolean): Unit = {
    val selectedNodes = getSelectedNodes(simulationPanel.getSelectedNodesIDs)
    if (selectedNodes.isEmpty && !selectionAttempted) {
      setNodesSensor(simulation.network.nodes.values, sensorName, value)
      simulation.setSensor(sensorName, value)
    } else {
      setNodesSensor(selectedNodes, sensorName, value)
      simulation.setSensor(sensorName, value.toString.toBoolean, selectedNodes)
    }
  }

  private def setNodesSensor(nodes: Iterable[Node], sensorName: String, value: Any): Unit =
    nodes.foreach(setNodeSensor(_, sensorName, value))

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D.handleNumberButtonPress]] */
  def handleNumberButtonPress(sensorIndex: Int): Unit = {
    getSensorName(sensorIndex).foreach(sensorName => {
      val selectedNodes = getSelectedNodes(simulationPanel.getSelectedNodesIDs)
      simulationPanel.getInitialSelectedNodeId
        .flatMap(initialNodeId => selectedNodes.find(_.id == initialNodeId))
        .fold()(initialNode => setSensorByInitialNode(sensorName, selectedNodes, initialNode))
    })
  }

  private def setSensorByInitialNode(sensorName: String, selectedNodes: Set[Node], initialNode: Node): Unit = {
    val sensorValue = initialNode.getSensorValue(sensorName)
    val newSensorValue = sensorValue match {case value: Boolean => !value}
    selectedNodes.foreach(node => setNodeSensor(node, sensorName, newSensorValue))
    simulation.setSensor(sensorName, newSensorValue, selectedNodes)
  }

  private def getSelectedNodes(selectedNodeIDs: Set[Int]): Set[Node] = {
    if(simulation.network == null){
      Set()
    } else {
      simulation.network.nodes.filter(node => selectedNodeIDs.contains(node._2.id)).values.toSet
    }
  }

  private def getSensorName(sensorIndex: Int): Option[String] = SensorEnum.fromInt(sensorIndex).map(_.name)

  private def setNodeSensor(node: Node, sensorName: String, newSensorValue: Any): Unit = {
    node.setSensor(sensorName, newSensorValue)
    nodeUpdater.updateNodeColorBySensors(node, simulationPanel)
  }
}

private[controller3d] object DefaultSensorSetter {
  def apply(simulationPanel: NetworkRenderer3D, simulation: Simulation, nodeUpdater: NodeUpdater): DefaultSensorSetter =
    new DefaultSensorSetter(simulationPanel, simulation, nodeUpdater)
}
