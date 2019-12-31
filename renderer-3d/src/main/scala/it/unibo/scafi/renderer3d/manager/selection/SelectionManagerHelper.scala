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

package it.unibo.scafi.renderer3d.manager.selection

import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.CullFace
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.shape.Shape3D
import scalafx.scene.{PerspectiveCamera, Scene}

/**
 * Helper object for [[SelectionManager]] with various utility methods.
 * */
private[selection] object SelectionManagerHelper {
  
  /**
   * Sets up the select volume, making it visible even when inside it and invisible at first.
   * @param selectVolume the shape to be set
   * */
  final def setupSelectVolume(selectVolume: Shape3D): Unit = {
    selectVolume.setCullFace(CullFace.NONE)
    selectVolume.setVisible(false)
  }

  /**
   * Finds out if the mouse cursor is over the current selection.
   * @param event the mouse event to check
   * @param selectVolume the current select volume, the cube that contains the selected nodes
   * @return whether the mouse cursor is over the current selection
   * */
  final def isMouseOnSelection(event: MouseEvent, selectVolume: Node): Boolean = {
    val pickedNode = event.getPickResult.getIntersectedNode
    pickedNode != null && areNodesIntersecting(pickedNode, selectVolume)
  }

  /**
   * Retrieves the nodes that are intersecting with the provided shape.
   * @param shape the shape that will be used to check the intersection with the nodes
   * @param networkNodes the set of networkNodes
   * @return the set of nodes that intersect with the shape
   * */
  final def getIntersectingNetworkNodes(shape: Shape3D, networkNodes: Set[NetworkNode]): Set[NetworkNode] =
    networkNodes.filter(areNodesIntersecting(shape, _))

  private final def areNodesIntersecting(node1: Node, node2: Node): Boolean =
    node1!= null && node2!=null && node1.getBoundsInParent.intersects(node2.getBoundsInParent)

  /**
   * Calculates the multiplier to apply to the movement vector that will be applied to the selected nodes. This makes
   * sure that the movement is perceived as the same with any window size, camera distance and field of view.
   * @param movementVector the initial movement vector taken from the movement of the mouse cursor
   * @param camera the PerspectiveCamera in the scene
   * @param initialNode the node that is in the center if the selection cube
   * @param scene the scene that contains all the nodes
   * */
  final def getMovementMultiplier(movementVector: Point2D, camera: PerspectiveCamera, initialNode: Option[NetworkNode],
                                  scene: Scene): Point2D = {
    val multiplier = camera.getFieldOfView / (60 * scene.getHeight)  *
      camera.getPosition.distance(initialNode.map(_.getNodePosition).getOrElse(Point3D.Zero))
    new Point2D(multiplier * movementVector.getX, multiplier * movementVector.getY)
  }
}
