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
import it.unibo.scafi.simulation.gui.controller.ControllerUtils.{formatAndRoundPosition, formatExport}
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.model.{Node, NodeValue}
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import it.unibo.scafi.space.Point3D

import scala.util.Try

/**
 * Utility object that has methods to update the scene nodes.
 * */
private[helper] object NodeUpdaterHelper {

  /** Checks the new node's position and if it's defined it updates the node's position, otherwise it creates the node.
   * @param newPosition the new node's position. It could be None, which means that the node doesn't exist yet in the UI
   * @param node the node to be created or moved
   * @param isPositionDifferent it says whether the new position is different from the previous one, for optimization
   * @param gui3d the 3D network renderer that has to be updated */
  def createOrMoveNode(newPosition: Option[Product3[Double, Double, Double]], node: Node, isPositionDifferent: Boolean,
                       gui3d: NetworkRenderer3D): Unit = {
    val nodeId = node.id.toString
    if (newPosition.isDefined) {
      if (isPositionDifferent) {
        gui3d.moveNode(nodeId, newPosition.getOrElse((0, 0, 0)))
      }
    } else {
      gui3d.addNode(node.position, nodeId)
    }
  }

  /**@param node the node that has to be checked
   * @param newPosition the new node's position. It could be None, which means that the node doesn't exist yet in the UI
   * @return whether the new node's position is different from the previous one or not */
  def didPositionChange(node: Node, newPosition: Option[Product3[Double, Double, Double]]): Boolean =
    newPosition.fold(false)(newPosition =>
        (node.position.x, node.position.y, node.position.z) != (newPosition._1, newPosition._2, newPosition._3))

  /** Retrieves the new node's position from the simulation.
   * @param node the node whose position has to be retrieved
   * @param gui3d the 3D network renderer
   * @param simulation the simulation that has to be read
   * @return the new node's position */
  def getUpdatedNodePosition(node: Node, gui3d: NetworkRenderer3D,
                             simulation: Simulation): Product3[Double, Double, Double] = {
    val vector = Try(Settings.Movement_Activator_3D(node.export)).getOrElse((0.0, 0.0, 0.0))
    val currentPosition = node.position
    (currentPosition.x + vector._1, currentPosition.y + vector._2, currentPosition.z + vector._3)
  }

  /** Sets the new node's position to the simulation.
   * @param node the node that has to be updated
   * @param position the new node's position
   * @param simulation the simulation that has to be read */
  def setSimulationNodePosition(node: Node, position: Product3[Double, Double, Double], simulation: Simulation): Unit = {
    node.position = new Point3D(position._1, position._2, position._3)
    simulation.setPosition(node)
  }

  /** Sets the new node's text.
   * @param node the node that has to be updated
   * @param valueTypeToShow the value type to be shown
   * @param gui3d the 3D network renderer */
  def updateNodeText(node: Node, valueTypeToShow: NodeValue)(implicit gui3d: NetworkRenderer3D): Unit = {
    val outputString = Try(Settings.To_String(node.export))
    if(outputString.isSuccess && !outputString.get.equals("")) {
      valueTypeToShow match {
        case NodeValue.ID => setNodeText(node, node.id.toString)
        case NodeValue.EXPORT => setNodeText(node, formatExport(node.export))
        case NodeValue.POSITION => setNodeText(node, formatAndRoundPosition(node.position))
        case NodeValue.POSITION_IN_GUI => gui3d.setNodeTextAsUIPosition(node.id.toString, formatAndRoundPosition)
        case NodeValue.SENSOR(name) => if(name != null) setNodeText(node, node.getSensorValue(name).toString)
        case _ => setNodeText(node, "")
      }
    }
  }

  private def setNodeText(node: Node, text: String)(implicit gui3d: NetworkRenderer3D): Unit =
    gui3d.setNodeText(node.id.toString, text) //updating the value of the node's label

  /** Checks if the specified node has Led_Activator active, if it does it enables a 3D sphere in the UI.
   * @param node the node that has to be checked
   * @param controller the 3D controller
   * @param gui3d the 3D network renderer */
  def updateLedActuatorStatus(node: Node, controller: Controller3D, gui3d: NetworkRenderer3D): Unit =
    if(controller.isLedActivatorSet){
      val enableLed = Try(Settings.Led_Activator(node.export)).getOrElse(false)
      gui3d.enableNodeFilledSphere(node.id.toString, enableLed)
    }

  /** Sets the node's color as the color relative to the first enabled sensor in that node.
   * @param node the node that has to be checked
   * @param gui3d the 3D network renderer */
  def updateNodeColorBySensors(node: Node, gui3d: NetworkRenderer3D): Unit = {
    val firstEnabledSensorInNode = node.sensors.find(_._2.equals(true)).map(_._1)
    val sensorColor = firstEnabledSensorInNode.map(SensorEnum.getColor(_).getOrElse(Settings.Color_device))
    gui3d.setNodeColor(node.id.toString, sensorColor.getOrElse(Settings.Color_device))
  }

  /** Sets the node's color by checking the observation function of the controller and the enabled sensor in that node.
   * @param node the node that has to be checked
   * @param gui3d the 3D network renderer
   * @param controller the 3D controller */
  def updateNodeColor(node: Node, gui3d: NetworkRenderer3D, controller: Controller3D): Unit = {
    if(controller.getObservation()(node.export)){
      gui3d.setNodeColor(node.id.toString, Settings.Color_observation)
    } else if(controller.isObservationSet) {
      updateNodeColorBySensors(node, gui3d)
    }
  }

}
