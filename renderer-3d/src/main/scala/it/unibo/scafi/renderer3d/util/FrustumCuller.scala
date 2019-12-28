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

package it.unibo.scafi.renderer3d.util

import java.util.{Timer, TimerTask}

import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import org.scalafx.extras._
import scalafx.scene.PerspectiveCamera
import scalafx.scene.transform.Rotate

/** This implements Frustum Culling, so that the Nodes not visible from the camera are not rendered, improving
 *  performance whenever the user is not viewing all the nodes.
 * @param camera the camera of the scene; this is used to calculate whether a node is visible from it or not
 * @param getNodes a function used to retrieve the nodes in the scene
 * */
class FrustumCuller(camera: PerspectiveCamera, getNodes: () => Set[NetworkNode]) extends Culler {

  private var timer = new Timer()

  /**
   * Starts to cull the node that are not visible.
   * */
  override def start(): Unit = {
    timer = new Timer()
    timer.schedule(new TimerTask {
      override def run(): Unit = updateCulling(camera, getNodes())
    }, 0, 150)
  }

  /**
   * Stops the culler. Use it if there is no more a need to hide scene nodes.
   * */
  override def stop(): Unit = timer.cancel()

  private def updateCulling(camera: PerspectiveCamera, nodes: Set[NetworkNode]): Unit = {
    val NODE_GROUP_SIZE = 250
    nodes.grouped(NODE_GROUP_SIZE).foreach(group => onFX {group.foreach(updateNodeVisibility(_, camera))})
  }

  private def updateNodeVisibility(node: NetworkNode, camera: PerspectiveCamera): Unit = {
    val cameraAngle = -camera.getYRotationAngle
    val cameraFacingDirection = FastMath.rotateVector(Rotate.XAxis, Rotate.YAxis, cameraAngle.toRadians)
    val cameraToNodeDirection = node.getNodePosition - camera.getPosition
    val angleBetweenDirections = cameraFacingDirection.angle(cameraToNodeDirection) - 170
    val isNodeInFrustum = angleBetweenDirections.abs < camera.getFieldOfView - 10
    if(node.isVisible != isNodeInFrustum) node.setVisible(isNodeInFrustum)
  }

}

object FrustumCuller {
  def apply(camera: PerspectiveCamera, getNodes: () => Set[NetworkNode]): FrustumCuller =
    new FrustumCuller(camera, getNodes)
}

trait Culler {

  def start(): Unit

  def stop(): Unit

}