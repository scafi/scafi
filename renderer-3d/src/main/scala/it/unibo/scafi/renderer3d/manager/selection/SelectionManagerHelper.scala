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

import java.util.concurrent.ThreadPoolExecutor

import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import it.unibo.scafi.renderer3d.util.math.MathUtils
import javafx.concurrent.Task
import javafx.scene
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.transform.Rotate
import scalafx.scene.{Camera, PerspectiveCamera, Scene}

/** Helper object for [[SelectionManager]] with various utility methods. */
private[selection] object SelectionManagerHelper {

  /** Finds out if the mouse cursor is over the current selection.
   * @param event the mouse event to check
   * @param selectVolume the current select volume, the cube that contains the selected nodes
   * @return whether the mouse cursor is over the current selection */
  final def isMouseOnSelection(event: MouseEvent, selectVolume: Node): Boolean = {
    val pickedNode = event.getPickResult.getIntersectedNode
    pickedNode != null && (pickedNode isIntersectingWith selectVolume)
  }

  /** Starts node selection, so that the user can select visible nodes. It shows a cube representing the selected area.
   * @param event the mouse event
   * @param state the current state of SelectionManager
   * @param scene the scene that contains all the nodes
   * @param selectVolume the cube that is going to be visible */
  final def startSelection(event: MouseEvent, state: SelectionManagerState, scene: Scene, selectVolume: Node): Unit =
    if(state.initialNode.isDefined && !scene.getChildren.contains(selectVolume)){
      selectVolume.moveTo(state.initialNode.map(_.getNodePosition).getOrElse(Point3D.Zero))
      selectVolume.setScale(1)
      selectVolume.setVisible(true)
      scene.getChildren.add(selectVolume)
    }

  /** Updates the selection volume so that it contains the selected nodes
   * @param selectVolume the selection volume to update
   * @param state the current state of SelectionManager
   * @param event the mouse event that caused this update
   * @param camera the camera in the scene */
  def updateSelectionVolume(selectVolume: Node, state: SelectionManagerState, event: MouseEvent, camera: Camera) {
    val initialNodeScreenPosition = state.initialNode.map(_.getScreenPosition).getOrElse(Point2D.Zero)
    val initialNodePosition = state.initialNode.map(_.getNodePosition).getOrElse(Point3D.Zero)
    val cameraToNodeDistance = initialNodePosition distance camera.getPosition
    selectVolume.setScale(event.getScreenPosition.distance(initialNodeScreenPosition) * cameraToNodeDistance / 1000)
  }

  /** Checks if the specified node should be updated or not.
   * @param event the current mouse event
   * @param node the node to check
   * @return whether the specified node should be updated */
  def shouldUpdateNode(event: MouseEvent, node: Node): Boolean =
    node.isVisible && (event.getScreenX + event.getScreenY) % 2 < 1

  /** Changes the length and height of selectVolume by the mouse movements.
   * @param selectVolume the node to modify
   * @param state the current state of SelectionManager
   * @param event the mouse event that caused this update
   * @param camera the camera in the scene */
  def changeSelectVolumeSizes(selectVolume: Node, state: SelectionManagerState, event: MouseEvent, camera: Camera) {
    val initialNodePosition = state.initialNode.map(_.getScreenPosition).getOrElse(Point2D.Zero)
    val positionDifference = (event.getScreenPosition subtract initialNodePosition) multiply 4
    if (isCameraMoreOnXAxis(camera, state)) {
      selectVolume.setScaleZ(positionDifference.getX)
    } else {
      selectVolume.setScaleX(positionDifference.getX)
    }
    selectVolume.setScaleY(positionDifference.getY)
  }

  private def isCameraMoreOnXAxis(camera: Camera, state: SelectionManagerState): Boolean = {
    val direction = camera.getPosition - state.initialNode.map(_.getNodePosition).getOrElse(Point3D.Zero)
    direction.getX.abs > direction.getZ.abs
  }

  /** Changes the length and height of selectVolume by the keyboard movements.
   * @param selectVolume the node to modify
   * @param state the current state of SelectionManager
   * @param vector the 2d vector specified by the user
   * @param camera the camera in the scene */
  def changeSelectVolumeSizes(selectVolume: Node, state: SelectionManagerState, vector: Point2D, camera: Camera) {
    val finalVector = vector.normalize() multiply  40
    if (isCameraMoreOnXAxis(camera, state)) {
      selectVolume.setScaleZ(finalVector.getX + selectVolume.getScaleZ)
    } else {
      selectVolume.setScaleX(finalVector.getX + selectVolume.getScaleX)
    }
    selectVolume.setScaleY(finalVector.getY + selectVolume.getScaleY)
  }

  /** Calculates the movement vector to apply to the selected nodes. It moves them in relation to the view of the camera.
   * @param event the mouse event
   * @param state the current state of SelectionManager
   * @param scene the scene that contains all the nodes
   * @param camera the camera in the scene
   * @return the movement vector */
  def getMovementVector(event: MouseEvent, state: SelectionManagerState, scene: Scene,
                        camera: PerspectiveCamera): Point3D = {
    val cameraRight = MathUtils.rotateVector(Rotate.XAxis, Rotate.YAxis, (-camera.getYRotationAngle - 90).toRadians)
    val mouseMovement = event.getScreenPosition subtract state.mousePosition.getOrElse(Point2D.Zero).delegate
    val movementVector = new Point2D(mouseMovement)
    val multiplier = camera.getFieldOfView / (60 * scene.getHeight)  *
      camera.getPosition.distance(state.initialNode.map(_.getNodePosition).getOrElse(Point3D.Zero))
    val movementMultiplier = new Point2D(multiplier * movementVector.getX, multiplier * movementVector.getY)
    (cameraRight * movementMultiplier.x) + Rotate.YAxis * movementMultiplier.y
  }

  /** Submits to the provided executor the movement task that is inside the provided SelectionManagerState instance.
   * @param executor the executor to work with
   * @param state the current state of SelectionManager */
  def submitMovementTaskToExecutor(executor: ThreadPoolExecutor, state: SelectionManagerState): Unit =
    executor.submit(new Task[Unit]() {
      override def call(): Unit = state.movementTask.getOrElse(() => ())()
    })

  /** Finds the NetworkNode in the screen closest to the mouse cursor.
   * @param event the mouse event
   * @param mainScene the scene that contains all the nodes
   * @param networkNodes the set of all the network nodes
   * @return the closest network node to the mouse cursor*/
  def findClosestNodeOnScreen(event: MouseEvent, mainScene: Scene,
                              networkNodes: Set[NetworkNode]): Option[NetworkNode] = { //use minByOption on scala 2.13
    val camera = mainScene.getCamera match {case camera: scene.PerspectiveCamera => camera}
    val filteredNodes = networkNodes.filter(camera.isNodeVisible(_, useSmallerFOVWindow = true))
    if(filteredNodes.isEmpty) None else Option(filteredNodes.minBy(_.getScreenPosition.distance(event.getScreenPosition)))
  }

  /** Makes the selection volume not visible.
   * @param scene the scene that contains all the nodes
   * @param selectVolume the selection volume to modify */
  def hideSelectVolume(scene: Scene, selectVolume: Node): Unit =
    {scene.getChildren.remove(selectVolume); selectVolume.setVisible(false)}
}
