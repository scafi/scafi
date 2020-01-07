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

import it.unibo.scafi.renderer3d.node.{NetworkNode, SimpleNetworkNode}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import scalafx.application.Platform
import scalafx.geometry.Point3D

/** Helper object for [[NodeManager]] with various utility methods. */
private[node] object NodeManagerHelper {

  /**
   * Adjusts the radius so that it's not negative.
   * @param radius the radius to adjust
   * @return the adjusted radius
   * */
  final def getAdjustedRadius(radius: Double): Double = if(radius > 0) radius else 0

  /**
   * Creates a new NetworkNode, with the parameters specified in the state of NodeManager.
   * @param position the position where the new node should be placed
   * @param UID the id of the node
   * @param state the current state of NodeManager
   * @return the new NetworkNode instance
   * */
  final def createNetworkNode(position: Product3[Double, Double, Double], UID: String,
                              state: NodeManagerState): NetworkNode = {
    val networkNode = SimpleNetworkNode(position.toPoint3D, UID, state.nodeLabelsScale)
    networkNode.setNodeColors(state.nodesColor.toScalaFx, state.movementColor.toScalaFx)
    networkNode.setSeeThroughSphereRadius(state.seeThroughSpheresRadius)
    networkNode.setFilledSphereRadius(state.filledSpheresRadius)
    networkNode.setFilledSphereColor(state.filledSpheresColor.toScalaFx)
    if (state.nodesScale != 1d) networkNode.setNodeScale(state.nodesScale)
    networkNode
  }

  /**
   * Sets the radius of the spheres related to the nodes.
   * @param state the current state of NodeManager
   * */
  final def setNodeSpheresRadius(state: NodeManagerState): Unit = {
    state.networkNodes.values.foreach(node => {
      node.setSeeThroughSphereRadius(state.seeThroughSpheresRadius)
      node.setFilledSphereRadius(state.filledSpheresRadius)
    })
  }

  /**
   * Moves the node to the specified position and renders it as a moving node.
   * @param position the position where the new node should be placed
   * @param showDirection whether the node should be rendered as a moving node or not
   * @param node the node to move
   * */
  def changeNodePosition(position: Product3[Double, Double, Double], showDirection: Boolean, node: NetworkNode) {
    if (showDirection) node.showMovement(show = true)
    node.moveNodeTo(position.toPoint3D, showDirection)
  }

  /**
   * Rotates all the labels so that they face the camera.
   * @param cameraPosition the position of the camera in the 3d space
   * @param state the current state of NodeManager
   * @param nodes the nodes to update. If empty, all the nodes will see their labels rotated.
   * */
  def rotateNodeLabels(cameraPosition: Point3D, state: NodeManagerState, nodes: Option[Set[NetworkNode]]): Unit = {
    val LABELS_GROUP_SIZE = 400
    val networkNodes = if(nodes.isEmpty) state.networkNodes.values else nodes.getOrElse(Set())
    networkNodes.grouped(LABELS_GROUP_SIZE)
      .foreach(group => Platform.runLater(group.foreach(_.rotateTextToCamera(cameraPosition))))
  }

}
