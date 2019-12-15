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

import it.unibo.scafi.renderer3d.manager.NetworkRenderer
import it.unibo.scafi.simulation.gui.controller.ControllerUtils.{formatExport, formatPosition, formatProductPosition}
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import it.unibo.scafi.simulation.gui.model.{Node, NodeValue}
import it.unibo.scafi.space.Point3D

import scala.util.Try

private[controller3d] object NodeUpdaterHelper {

  def createOrMoveNode(newPosition: Option[Product3[Double, Double, Double]], node: Node, isPositionDifferent: Boolean,
                       gui3d: NetworkRenderer): Unit = {
    val nodeId = node.id.toString
    if (newPosition.isDefined) {
      if (isPositionDifferent) {
        gui3d.moveNode(nodeId, newPosition.getOrElse((0, 0, 0)))
      }
    } else {
      gui3d.addNode(node.position, nodeId)
    }
  }

  def didPositionChange(node: Node, newPosition: Option[Product3[Double, Double, Double]]): Boolean =
    newPosition.fold(false)(newPosition =>
        (node.position.x, node.position.y, node.position.z) != (newPosition._1, newPosition._2, newPosition._3))

  def getUpdatedNodePosition(node: Node, gui3d: NetworkRenderer,
                             simulation: Simulation): Product3[Double, Double, Double] = {
    val vector = Try(Settings.Movement_Activator_3D(node.export)).getOrElse((0.0, 0.0, 0.0))
    val currentPosition = node.position
    (currentPosition.x + vector._1, currentPosition.y + vector._2, currentPosition.z + vector._3)
  }

  def setSimulationNodePosition(node: Node, position: Product3[Double, Double, Double], simulation: Simulation): Unit = {
    simulation.setPosition(node)
    node.position = new Point3D(position._1, position._2, position._3)
  }

  def updateNodeText(node: Node, valueToShow: NodeValue)(implicit gui3d: NetworkRenderer): Unit = {
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

  private def setNodeText(node: Node, text: String)(implicit gui3d: NetworkRenderer): Unit =
    gui3d.setNodeText(node.id.toString, text) //updating the value of the node's label

  def updateLedActuatorRadius(node: Node, controller: Controller3D, gui3d: NetworkRenderer): Unit =
    if(controller.isLedActivatorSet){
      val enableLed = Try(Settings.Led_Activator(node.export)).getOrElse(false)
      gui3d.enableNodeFilledSphere(node.id.toString, enableLed)
    }

  def updateNodeColorBySensors(node: Node, simulationPanel: NetworkRenderer): Unit = {
    val firstEnabledSensorInNode = node.sensors.find(_._2.equals(true)).map(_._1)
    val sensorColor = firstEnabledSensorInNode.map(SensorEnum.getColor(_).getOrElse(Settings.Color_device))
    simulationPanel.setNodeColor(node.id.toString, sensorColor.getOrElse(Settings.Color_device))
  }

  def updateNodeColor(node: Node, gui3d: NetworkRenderer, controller: Controller3D): Unit = {
    if(controller.getObservation()(node.export)){
      gui3d.setNodeColor(node.id.toString, Settings.Color_observation)
    } else if(controller.isObservationSet) {
      updateNodeColorBySensors(node, gui3d)
    }
  }

}
