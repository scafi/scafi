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

package it.unibo.scafi.renderer3d.manager.node

import com.typesafe.scalalogging.Logger
import it.unibo.scafi.renderer3d.manager.connection.ConnectionManager
import it.unibo.scafi.renderer3d.manager.node.NodeManagerHelper._
import it.unibo.scafi.renderer3d.manager.scene.SceneManager
import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import org.fxyz3d.geometry.MathUtils
import org.scalafx.extras._
import scalafx.scene.Scene

/** Trait that contains some of the main API of the renderer-3d module: the methods that create or modify nodes. */
private[manager] trait NodeManager {
  this: ConnectionManager with SceneManager => //ConnectionManager and SceneManager have to be mixed in with NodeManager

  private[this] final var state: NodeManagerState = NodeManagerState()
  private[this] final val logger = Logger("NodeManager")
  protected val mainScene: Scene

  protected final def rotateNodeLabelsIfNeeded(nodes: Option[Set[NetworkNode]] = None): Unit = onFX {
    val cameraPosition = mainScene.getCamera.getPosition
    if(nodes.nonEmpty || cameraPosition.distance(state.positionThatLabelsFace) > sceneSize/4){
      rotateNodeLabels(cameraPosition, state, nodes); state = state.copy(positionThatLabelsFace = cameraPosition)
    } //rotates all the nodes if "nodes" is empty
  }

  /** Enables or disables the colored sphere (not the outlined one) centered on the specified node.
   * @param nodeUID the id of the affected node
   * @param enable specifies if the method should enable or disable the sphere */
  final def enableNodeFilledSphere(nodeUID: String, enable: Boolean): Unit = onFX {
    val FILLED_SPHERES_RADIUS = 8
    findNodeAndAct(nodeUID, _.setFilledSphereRadius(if(enable) FILLED_SPHERES_RADIUS else 0))
  }

  /** Sets the radius of the colored spheres and the outlined ones, centered on the nodes.
   * @param seeThroughSpheresRadius the radius of the outlined spheres
   * @param filledSpheresRadius the radius of the colored spheres */
  final def setSpheresRadius(seeThroughSpheresRadius: Double, filledSpheresRadius: Double): Unit = onFX {
    state = state.copy(seeThroughSpheresRadius = getAdjustedRadius(seeThroughSpheresRadius),
      filledSpheresRadius = getAdjustedRadius(filledSpheresRadius))
    setNodeSpheresRadius(state)
  }

  /** Adds a new 3D node at the specified position and with the specified ID.
   * @param position the position where the new node will be placed
   * @param UID the unique id of the new node */
  final def addNode(position: Product3[Double, Double, Double], UID: String): Unit = onFX {
    findNode(UID).fold{
      val networkNode = createNetworkNode(position, UID, state)
      state = state.copy(networkNodes = state.networkNodes + (UID -> networkNode))
      mainScene.getChildren.add(networkNode); ()
    }(_ => logger.error("Node " + UID + " already exists"))
  }

  /** Removes the node with the specified ID, so that it's not in the scene anymore.
   * @param nodeUID the unique id of the node to remove */
  final def removeNode(nodeUID: String): Unit = findNodeAndAct(nodeUID, node => {
    state = state.copy(networkNodes = state.networkNodes - nodeUID)
    mainScene.getChildren.remove(node)
    removeAllNodeConnections(node.UID) //using ConnectionManager
  })

  private final def findNodeAndAct(nodeUID: String, action: NetworkNode => Unit): Unit =
    onFX(findNode(nodeUID).fold(logger.error("Could not find node " + nodeUID))(node => {action(node.toNetworkNode)}))

  protected final def findNode(nodeUID: String): Option[NetworkNode] = state.networkNodes.get(nodeUID)

  /** Moves the node with the specified ID to the specified position.
   * @param nodeUID the unique id of the node to move
   * @param position the new position to set
   * @param showDirection whether the view should show the movement direction or not */
  final def moveNode(nodeUID: String, position: Product3[Double, Double, Double], showDirection: Boolean): Unit =
    findNodeAndAct(nodeUID, node => {changeNodePosition(position, showDirection, node); updateNodeConnections(nodeUID)})

  /** Shows the node as a normal, non moving node.
   * @param nodeUID the unique id of the node to move */
  final def stopShowingNodeMovement(nodeUID: String): Unit = findNodeAndAct(nodeUID, _.hideMovement())

  /** Sets the label text of the specified node to the new text
   * @param nodeUID the unique id of the node
   * @param text the new text to set */
  final def setNodeText(nodeUID: String, text: String): Unit = findNodeAndAct(nodeUID, _.setText(text, mainScene.getCamera))

  /** Sets the label text of the specified node to the position of that node in 2D, from the point of view of the camera
   * @param nodeUID the unique id of the node
   * @param positionFormatter the function used to format the position before setting it as the label's text */
  final def setNodeTextAsUIPosition(nodeUID: String, positionFormatter: Product2[Double, Double] => String): Unit =
    findNodeAndAct(nodeUID, node => {val position = node.getScreenPosition
      node.setText(positionFormatter(position.x, position.y), mainScene.getCamera)})

  /** Sets the color of the specified node to a new one.
   * @param nodeUID the unique id of the node
   * @param color the new color of the node */
  final def setNodeColor(nodeUID: String, color: java.awt.Color): Unit =
    findNodeAndAct(nodeUID, _.setNodeColor(color.toScalaFx))

  /** Sets the default and movement colors of all the nodes. This applies to nodes not already created, too.
   * @param defaultColor the new default color for the nodes
   * @param movementColor the new movement color for the nodes */
  final def setNodesColors(defaultColor: java.awt.Color, movementColor: java.awt.Color): Unit = onFX {
    state = state.copy(nodesColor = defaultColor, movementColor = movementColor)
    state.networkNodes.values.foreach(node => node.setNodeColors(defaultColor.toScalaFx, movementColor.toScalaFx))
  }

  /** Sets the color of all the colored spheres centered on the nodes. This applies to spheres not already created, too.
   * @param color the new default color for the spheres */
  final def setFilledSpheresColor(color: java.awt.Color): Unit =
    onFX {state = state.copy(filledSpheresColor = color)
    state.networkNodes.values.foreach(_.setFilledSphereColor(color.toScalaFx))}

  /** Sets the scale of all the nodes (and also their labels). This applies to nodes not already created, too.
   * @param scale the new scale of the nodes */
  final def setNodesScale(scale: Double): Unit = onFX {
    val finalScale = scale * sceneScaleMultiplier
    state = state.copy(nodesScale = finalScale, nodeLabelsScale = finalScale)
    state.networkNodes.values.foreach(_.setNodeScale(state.nodesScale))
    updateLabelsSize(0)
  }

  protected final def getAllNetworkNodes: Set[NetworkNode] = state.networkNodes.values.toSet

  /** Increases the font size of every label. This applies to labels not already created, too. */
  final def increaseFontSize(): Unit = updateLabelsSize(0.1*sceneScaleMultiplier)

  /** Decreases the font size of every label. This applies to labels not already created, too. */
  final def decreaseFontSize(): Unit = updateLabelsSize(-0.1*sceneScaleMultiplier)

  private final def updateLabelsSize(sizeDifference: Double): Unit = onFX {
    val (minScale, maxScale) = (0.7*sceneScaleMultiplier, 1.2*sceneScaleMultiplier)
    state = state.copy(nodeLabelsScale = MathUtils.clamp(state.nodeLabelsScale + sizeDifference, minScale, maxScale))
    state.networkNodes.values.foreach(_.setLabelScale(state.nodeLabelsScale))
  }
}