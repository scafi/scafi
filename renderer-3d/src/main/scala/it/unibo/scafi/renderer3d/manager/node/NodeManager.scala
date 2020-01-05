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
import it.unibo.scafi.renderer3d.manager.scene.SceneManager
import it.unibo.scafi.renderer3d.node.{NetworkNode, SimpleNetworkNode}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import org.fxyz3d.geometry.MathUtils
import org.scalafx.extras._
import scalafx.application.Platform
import scalafx.scene.{Camera, Scene}

import scala.collection.mutable.{Map => MutableMap}

/** Trait that contains some of the main API of the renderer-3d module: the methods that create or modify nodes. */
private[manager] trait NodeManager {
  this: ConnectionManager with SceneManager => //ConnectionManager and SceneManager have to be mixed in with NodeManager

  private[this] final val networkNodes = MutableMap[String, NetworkNode]()
  private[this] final var state: NodeManagerState = NodeManagerState()
  private[this] final val logger = Logger("NodeManager")
  protected val mainScene: Scene

  protected final def rotateNodeLabelsIfNeeded(camera: Camera): Unit = onFX {
    val LABELS_GROUP_SIZE = 400
    val cameraPosition = camera.getPosition
    if(cameraPosition.distance(state.positionThatLabelsFace) > sceneSize/4){
      networkNodes.values.grouped(LABELS_GROUP_SIZE)
        .foreach(group => Platform.runLater(group.foreach(_.rotateTextToCamera(cameraPosition))))
      state = state.copy(positionThatLabelsFace = cameraPosition)
    }
  }

  /** Enables or disables the colored sphere (not the outlined one) centered on the specified node.
   * @param nodeUID the id of the affected node
   * @param enable specifies if the method should enable or disable the sphere
   * @return Unit, since it has the side effect of enabling or disabling the sphere */
  final def enableNodeFilledSphere(nodeUID: String, enable: Boolean): Unit = onFX {
    val FILLED_SPHERES_RADIUS = 8
    findNodeAndAct(nodeUID, _.setFilledSphereRadius(if(enable) FILLED_SPHERES_RADIUS else 0))
  }

  /** Sets the radius of the colored spheres and the outlined ones, centered on the nodes.
   * @param seeThroughSpheresRadius the radius of the outlined spheres
   * @param filledSpheresRadius the radius of the colored spheres
   * @return Unit, since it has the side effect of setting the radius of the spheres */
  final def setSpheresRadius(seeThroughSpheresRadius: Double, filledSpheresRadius: Double): Unit = onFX {
    state = state.copy(seeThroughSpheresRadius = getAdjustedRadius(seeThroughSpheresRadius),
      filledSpheresRadius = getAdjustedRadius(filledSpheresRadius))
    networkNodes.values.foreach(node => {
      node.setSeeThroughSphereRadius(state.seeThroughSpheresRadius)
      node.setFilledSphereRadius(state.filledSpheresRadius)
    })
  }

  private final def getAdjustedRadius(radius: Double): Double = if(radius > 0) radius else 0

  /** Adds a new 3D node at the specified position and with the specified ID.
   * @param position the position where the new node will be placed
   * @param UID the unique id of the new node
   * @return Unit, since it has the side effect of adding the new node */
  final def addNode(position: Product3[Double, Double, Double], UID: String): Unit = onFX {
    findNode(UID).fold{
      val networkNode = SimpleNetworkNode(position.toPoint3D, UID, state.nodesColor.toScalaFx, state.nodeLabelsScale)
      networkNode.setSeeThroughSphereRadius(state.seeThroughSpheresRadius)
      networkNode.setFilledSphereRadius(state.filledSpheresRadius)
      networkNode.setFilledSphereColor(state.filledSpheresColor.toScalaFx)
      networkNode.setSelectionColor(state.selectionColor.toScalaFx)
      if(state.nodesScale != 1d) networkNode.setNodeScale(state.nodesScale)
      networkNodes(UID) = networkNode
      mainScene.getChildren.add(networkNode); ()
    }(_ => logger.error("Node " + UID + " already exists"))
  }

  /** Removes the node with the specified ID, so that it's not in the scene anymore.
   * @param nodeUID the unique id of the node to remove
   * @return Unit, since it has the side effect of removing the node */
  final def removeNode(nodeUID: String): Unit = findNodeAndAct(nodeUID, node => {
      networkNodes.remove(nodeUID)
      mainScene.getChildren.remove(node)
      removeAllNodeConnections(node.UID) //using ConnectionManager
    })

  private final def findNodeAndAct(nodeUID: String, action: NetworkNode => Unit): Unit =
    onFX(findNode(nodeUID).fold(logger.error("Could not find node " + nodeUID))(node => {action(node.toNetworkNode)}))

  protected final def findNode(nodeUID: String): Option[NetworkNode] = networkNodes.get(nodeUID)

  /** Moves the node with the specified ID to the specified position.
   * @param nodeUID the unique id of the node to move
   * @param position the new position to set
   * @return Unit, since it has the side effect of moving the node */
  final def moveNode(nodeUID: String, position: Product3[Double, Double, Double]): Unit =
    findNodeAndAct(nodeUID, node => {node.moveNodeTo(position.toPoint3D); updateNodeConnections(nodeUID)})

  /** Sets the label text of the specified node to the new text
   * @param nodeUID the unique id of the node
   * @param text the new text to set
   * @return Unit, since it has the side effect of changing the label's text */
  final def setNodeText(nodeUID: String, text: String): Unit = findNodeAndAct(nodeUID, _.setText(text, mainScene.getCamera))

  /** Sets the label text of the specified node to the position of that node in 2D, from the point of view of the camera
   * @param nodeUID the unique id of the node
   * @param positionFormatter the function used to format the position before setting it as the label's text
   * @return Unit, since it has the side effect of changing the label's text */
  final def setNodeTextAsUIPosition(nodeUID: String, positionFormatter: Product2[Double, Double] => String): Unit =
    findNodeAndAct(nodeUID, node => {val position = node.getScreenPosition
      node.setText(positionFormatter(position.x, position.y), mainScene.getCamera)})

  /** Sets the color of the specified node to a new one.
   * @param nodeUID the unique id of the node
   * @param color the new color of the node
   * @return Unit, since it has the side effect of changing the node's color */
  final def setNodeColor(nodeUID: String, color: java.awt.Color): Unit =
    findNodeAndAct(nodeUID, _.setNodeColor(color.toScalaFx))

  /** Sets the default color of all the nodes. This applies to nodes not already created, too.
   * @param color the new default color for the nodes
   * @return Unit, since it has the side effect of changing the nodes' color */
  final def setNodesColor(color: java.awt.Color): Unit =
    onFX {state = state.copy(nodesColor = color); networkNodes.values.foreach(_.setNodeColor(color.toScalaFx))}

  /** Sets the color of all the colored spheres centered on the nodes. This applies to spheres not already created, too.
   * @param color the new default color for the spheres
   * @return Unit, since it has the side effect of changing the spheres' color */
  final def setFilledSpheresColor(color: java.awt.Color): Unit = onFX {
    state = state.copy(filledSpheresColor = color); networkNodes.values.foreach(_.setFilledSphereColor(color.toScalaFx))}

  /** Sets the scale of all the nodes (and also their labels). This applies to nodes not already created, too.
   * @param scale the new scale of the nodes
   * @return Unit, since it has the side effect of changing the nodes' scale */
  final def setNodesScale(scale: Double): Unit = onFX {
    val finalScale = scale * sceneScaleMultiplier
    state = state.copy(nodesScale = finalScale, nodeLabelsScale = finalScale)
    networkNodes.values.foreach(_.setNodeScale(state.nodesScale))
    updateLabelsSize(0)
  }

  protected final def getAllNetworkNodes: Set[NetworkNode] = networkNodes.values.toSet

  /** Increases the font size of every label. This applies to labels not already created, too.
   * @return Unit, since it has the side effect of changing the font size of the labels */
  final def increaseFontSize(): Unit = updateLabelsSize(0.1*sceneScaleMultiplier)

  /** Decreases the font size of every label. This applies to labels not already created, too.
   * @return Unit, since it has the side effect of changing the font size of the labels */
  final def decreaseFontSize(): Unit = updateLabelsSize(-0.1*sceneScaleMultiplier)

  private final def updateLabelsSize(sizeDifference: Double): Unit = onFX {
    val (minScale, maxScale) = (0.7*sceneScaleMultiplier, 1.2*sceneScaleMultiplier)
    state = state.copy(nodeLabelsScale = MathUtils.clamp(state.nodeLabelsScale + sizeDifference, minScale, maxScale))
    networkNodes.values.foreach(_.setLabelScale(state.nodeLabelsScale))
  }

  /** Sets the color of the nodes when they get selected. This applies to nodes not already created, too.
   * @param color the new color of the selected nodes
   * @return Unit, since it has the side effect of changing the nodes' selection color */
  final def setSelectionColor(color: java.awt.Color): Unit = onFX {
    state = state.copy(selectionColor = color)
    getAllNetworkNodes.foreach(_.setSelectionColor(color.toScalaFx))
  }
}