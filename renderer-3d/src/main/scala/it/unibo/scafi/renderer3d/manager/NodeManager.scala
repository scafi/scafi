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

package it.unibo.scafi.renderer3d.manager

import com.typesafe.scalalogging.Logger
import it.unibo.scafi.renderer3d.node.{NetworkNode, SimpleNetworkNode}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import org.scalafx.extras._
import scalafx.application.Platform
import scalafx.scene.{Camera, Scene}

import scala.collection.mutable.{Map => MutableMap}

private[manager] trait NodeManager {
  this: ConnectionManager => //ConnectionManager has to also be mixed in with NodeManager

  private[this] final val FILLED_SPHERE_RADIUS = 100
  private[this] final val networkNodes = MutableMap[String, NetworkNode]()
  private[this] final var state = NodeManagerState()
  private[this] val logger = Logger("NodeManager")
  protected val mainScene: Scene

  protected final def rotateNodeLabelsIfNeeded(camera: Camera): Unit = onFX {
    val LABELS_GROUP_SIZE = 150
    val cameraPosition = camera.getPosition
    if(cameraPosition.distance(state.positionThatLabelsFace) > state.sceneSize/3.33){
      networkNodes.values.grouped(LABELS_GROUP_SIZE)
        .foreach(group => Platform.runLater(group.foreach(_.rotateTextToCamera(cameraPosition))))
      state = state.setPositionThatLabelsFace(cameraPosition)
    }
  }

  final def enableNodeFilledSphere(nodeUID: String, enable: Boolean): Unit =
    onFX {findNodeAndAct(nodeUID, _.setFilledSphereRadius(if(enable) FILLED_SPHERE_RADIUS else 0))}

  final def setSpheresRadius(seeThroughSpheresRadius: Double, filledSpheresRadius: Double): Unit = onFX {
    state = state.copy(seeThroughSpheresRadius = getAdjustedRadius(seeThroughSpheresRadius),
      filledSpheresRadius = getAdjustedRadius(filledSpheresRadius))
    networkNodes.values.foreach(node => {
      node.setSeeThroughSphereRadius(state.seeThroughSpheresRadius)
      node.setFilledSphereRadius(state.filledSpheresRadius)
    })
  }

  private final def getAdjustedRadius(radius: Double): Double = if(radius > 0) radius else 0

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

  final def removeNode(nodeUID: String): Unit = findNodeAndAct(nodeUID, node => {
      networkNodes.remove(nodeUID)
      mainScene.getChildren.remove(node)
      removeAllNodeConnections(node.getId) //using ConnectionManager
    })

  private final def findNodeAndAct(nodeUID: String, action: NetworkNode => Unit): Unit =
    onFX(findNode(nodeUID).fold(logger.error("Could not find node " + nodeUID))(node => {action(node.toNetworkNode)}))

  protected final def findNode(nodeUID: String): Option[NetworkNode] = networkNodes.get(nodeUID)

  final def moveNode(nodeUID: String, position: Product3[Double, Double, Double]): Unit = //TODO: this should not get called if the nodes don't move
    findNodeAndAct(nodeUID, node => {node.moveNodeTo(position.toPoint3D); updateNodeConnections(nodeUID)})

  final def setNodeText(nodeUID: String, text: String): Unit = findNodeAndAct(nodeUID, _.setText(text))

  final def setNodeTextAsUIPosition(nodeUID: String, positionFormatter: Product2[Double, Double] => String): Unit =
    findNodeAndAct(nodeUID, node =>
    {val position = node.getScreenPosition; node.setText(positionFormatter((position.x, position.y)))})

  final def setNodeColor(nodeUID: String, color: java.awt.Color): Unit =
    findNodeAndAct(nodeUID, _.setNodeColor(color.toScalaFx))

  final def setNodesColor(color: java.awt.Color): Unit =
    onFX {state = state.setNodesColor(color); networkNodes.values.foreach(_.setNodeColor(color.toScalaFx))}

  final def setFilledSpheresColor(color: java.awt.Color): Unit = onFX {
    state = state.setFilledSpheresColor(color)
    networkNodes.values.foreach(_.setFilledSphereColor(color.toScalaFx))
  }

  final def setNodesScale(scale: Double): Unit =
    onFX {state = state.setNodesScale(scale); networkNodes.values.foreach(_.setNodeScale(scale))}

  protected final def getAllNetworkNodes: Set[NetworkNode] = networkNodes.values.toSet

  final def increaseFontSize(): Unit = updateLabelSize(0.1)

  final def decreaseFontSize(): Unit = updateLabelSize(-0.1)

  private final def updateLabelSize(sizeDifference: Double): Unit = onFX {
    val MIN_SCALE = 0.5
    val MAX_SCALE = 1.5
    state = state.setNodeLabelsScale(RichMath.clamp(state.nodeLabelsScale + sizeDifference, MIN_SCALE, MAX_SCALE))
    networkNodes.values.foreach(_.setLabelScale(state.nodeLabelsScale))
  }

  final def setSelectionColor(color: java.awt.Color): Unit = onFX {
    state = state.setSelectionColor(color)
    getAllNetworkNodes.foreach(_.setSelectionColor(color.toScalaFx))
  }

  final def setSceneSize(sceneSize: Double): Unit = onFX {state = state.setSceneSize(sceneSize)}
}