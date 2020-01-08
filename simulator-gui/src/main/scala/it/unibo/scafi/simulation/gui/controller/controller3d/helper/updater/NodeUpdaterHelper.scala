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

import java.awt.Color

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.gui.controller.ControllerUtils._
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.controller.controller3d.helper.PositionConverter
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.model.{Node, NodeValue}
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import it.unibo.scafi.space.Point3D

import scala.util.Try

/** Utility object that has methods to update the scene nodes. */
private[updater] object NodeUpdaterHelper {

  /** Checks the new node's position and if it's defined it updates the node's position, otherwise it creates the node.
   * @param newPosition the new node's position. It could be None, which means that the node doesn't exist yet in the UI
   * @param node the node to be created or moved
   * @param options specifies how to update the node's position and movement
   * @param gui3d the 3D network renderer that has to be updated */
  def createOrMoveNode(newPosition: Option[Product3[Double, Double, Double]], node: Node, options: UpdateOptions,
                       gui3d: NetworkRenderer3D): Unit = {
    val nodeId = node.id.toString //TODO: don't do this in javaFx thread
    newPosition.fold(gui3d.addNode(PositionConverter.controllerToView(node.position), nodeId))(newPosition =>
      if (options.isPositionNew) {
        gui3d.moveNode(nodeId, PositionConverter.controllerToView(newPosition), showDirection = options.showMoveDirection)
      } else if(options.stoppedMoving) {
        gui3d.stopShowingNodeMovement(nodeId)
      })
  }

  /**@param node the node that has to be checked
   * @param newPosition the new node's position. It could be None, which means that the node doesn't exist yet in the UI
   * @return whether the new node's position is different from the previous one or not */
  def didPositionChange(node: Node, newPosition: Option[Product3[Double, Double, Double]]): Boolean =
    newPosition.fold(false)(newPosition =>
        (node.position.x, node.position.y, node.position.z) != (newPosition._1, newPosition._2, newPosition._3))

  /** Retrieves the new node's position from the simulation. Uses the 2d movement function if the 3d one is not defined,
   * by applying it to the x and y of the nodes.
   * @param node the node whose position has to be retrieved
   * @param gui3d the 3D network renderer
   * @param simulation the simulation that has to be read
   * @return the new node's position */
  def getUpdatedNodePosition(node: Node, gui3d: NetworkRenderer3D,
                             simulation: Simulation): Product3[Double, Double, Double] = {
    val movement2dTo3D =
      (anyObject: Any) => {val position2d = Settings.Movement_Activator(anyObject); (position2d._1, position2d._2, 0d)}
    val movementFunction = Settings.Movement_Activator_3D.fold(movement2dTo3D)(function => function)
    val vector = Try(movementFunction(node.export)).getOrElse((0.0, 0.0, 0.0))
    val currentPosition = node.position
    (currentPosition.x + vector._1, currentPosition.y + vector._2, currentPosition.z + vector._3)
  }

  /** Sets the new node's position to the simulation.
   * @param node the node that has to be updated
   * @param position the new node's position
   * @param simulation the simulation that has to be read */
  def setSimulationNodePosition(node: Node, position: Product3[Double, Double, Double], simulation: Simulation) {
    node.position = new Point3D(position._1, position._2, position._3)
    simulation.setPosition(node)
  }

  /** Sets the new node's text.
   * @param node the node that has to be updated
   * @param valueTypeToShow the value type to be shown
   * @param gui3d the 3D network renderer */
  def updateNodeText(node: Node, valueTypeToShow: NodeValue)(implicit gui3d: NetworkRenderer3D): Unit = {
    val outputString = Try(Settings.To_String(node.export)) //TODO: don't do this in javaFx thread
    if(outputString.isSuccess && !outputString.getOrElse("").equals("")) {
      valueTypeToShow match {
        case NodeValue.ID => setNodeText(node, node.id.toString)
        case NodeValue.EXPORT => setNodeText(node, formatExport(node.export))
        case NodeValue.POSITION => setNodeText(node, formatPosition(node.position))
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
      val enableLed = Try(Settings.Led_Activator(node.export)).getOrElse(false) //TODO: don't do this in javaFx thread
      gui3d.enableNodeFilledSphere(node.id.toString, enableLed)
    }

  /** See [[NodeUpdater.updateNodeColorBySensors]] */
  def updateNodeColorBySensors(node: Node, gui3d: NetworkRenderer3D): Unit =
    gui3d.setNodeColor(node.id.toString, getNodeColorBySensors(node))

  private def getNodeColorBySensors(node: Node): Color = {
    val firstEnabledSensorInNode = node.sensors.find(_._2.equals(true)).map(_._1)
    firstEnabledSensorInNode.flatMap(SensorEnum.getColor).getOrElse(Settings.Color_device)
  }

  /** Gets the new color for the provided node.
   * @param node the node whose color has to be decided
   * @param controller the 3D controller
   * @return the new color for the node or None if the node's color is still up to date */
  def getUpdatedNodeColor(node: Node, controller: Controller3D): Option[Color] =
    if(controller.getObservation()(node.export)){
      Option(Settings.Color_observation)
    } else if(controller.isObservationSet) {
      Option(getNodeColorBySensors(node))
    } else {
      None
    }

  /** Given a set of node IDs and a movement vector, it returns a set containing the nodes and their new positions.
   * @param nodeIDs the set of node IDs
   * @param movement the movement vector
   * @param simulation the simulation that has to be read
   * @return a set containing the nodes and their new positions */
  def getMovedNodes(nodeIDs: Set[String], movement: Product3[Double, Double, Double],
                    simulation: Simulation): Set[(Node, Option[(Double, Double, Double)])] = {
    val simulationNodes = nodeIDs.map(nodeID => simulation.network.nodes(nodeID.toInt)).map(node => {
      val nodePosition = node.position
      val vector = PositionConverter.viewToController(movement)
      (node, Option(nodePosition.x + vector._1, nodePosition.y + vector._2, nodePosition.z + vector._3))
    })
    simulationNodes
  }

}
