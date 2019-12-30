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

import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.{FastMath, RunOnExecutor}
import it.unibo.scafi.renderer3d.util.Rendering3DUtils._
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.CullFace
import org.scalafx.extras._
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Shape3D
import scalafx.scene.transform.Rotate
import scalafx.scene.{PerspectiveCamera, Scene}

/** Trait that contains some of the main API of the renderer-3d module regarding the nodes' selection. */
private[manager] trait SelectionManager {
  this: NodeManager => //NodeManager has to also be mixed in with SelectionManager

  protected val mainScene: Scene
  private[this] val selectVolume = createCube(1, Color.color(0.2, 0.2, 0.8, 0.5))
  private[this] var state = SelectionManagerState()

  selectVolume.setCullFace(CullFace.NONE)
  selectVolume.setVisible(false)

  protected final def setSelectionVolumeCenter(event: MouseEvent): Unit = onFX {
    val screenPosition = new Point2D(event.getScreenX, event.getScreenY) //TODO: remove copy-paste
    val allNetworkNodes = getAllNetworkNodes
    if(allNetworkNodes.isEmpty){
      state = state.copy(initialNode = None)
    } else {
      state = state.copy(initialNode = Option(allNetworkNodes.minBy(_.getScreenPosition.distance(screenPosition))))
    }
    state = state.copy(selectionComplete = false)
    deselectSelectedNodes()
  }

  protected final def setInitialMousePosition(event: MouseEvent): Unit =
    onFX {state = state.copy(initialMousePosition = new Point2D(event.getScreenX, event.getScreenY)}

  protected final def startSelection(event: MouseEvent): Unit = onFX {
    if(state.initialNode.isDefined && !mainScene.getChildren.contains(selectVolume)){
      selectVolume.moveTo(state.initialNode.map(_.getNodePosition).getOrElse(Point3D.Zero))
      selectVolume.setScale(1)
      selectVolume.setVisible(true)
      mainScene.getChildren.add(selectVolume)
    }
  }

  private final def deselectSelectedNodes(): Unit = {
    state.selectedNodes.foreach(_.deselect())
    state = state.copy(selectedNodes = Set())
  }

  protected final def moveSelectedNodes(camera: PerspectiveCamera, event: MouseEvent): Unit = onFX {
    val cameraYAngle = -camera.getYRotationAngle
    val cameraRight = FastMath.rotateVector(Rotate.XAxis, Rotate.YAxis, (cameraYAngle + 90).toRadians)
    val movement = new Point2D(event.getScreenX, event.getScreenY) subtract state.initialMousePosition
    val multiplier = new Point2D(camera.getFieldOfView/40*mainScene.getWindow.getWidth/25600*movement.getX,
      camera.getFieldOfView/40*mainScene.getWindow.getHeight/14400*movement.getY)
    val movementVector = (cameraRight * multiplier.x) + Rotate.YAxis*multiplier.y
    state.selectedNodes.foreach(node => moveNode(node.UID, (node.getNodePosition + movementVector).toProduct))
    selectVolume.moveTo(selectVolume.getPosition + movementVector)
    RunOnExecutor {state.movementAction(state.selectedNodes.map(node => (node.UID, node.getNodePosition.toProduct)))}
  }

  protected final def modifySelectionVolume(camera: Node, event: MouseEvent): Unit = onFX {
    if(selectVolume.isVisible){
      val screenPosition = event.getScreenPosition
      val initialNodeScreenPosition = state.initialNode.map(_.getScreenPosition).getOrElse(Point2D.Zero)
      val cameraToNodeDistance = state.initialNode.map(_.getPosition).getOrElse(Point3D.Zero).distance(camera.getPosition)
      selectVolume.setScale(screenPosition.distance(initialNodeScreenPosition) * cameraToNodeDistance / 1000)
      selectVolume.lookAtOnXZPlane(camera.getPosition)
      updateSelectionIfNeeded(event)
    }
  }

  private final def updateSelectionIfNeeded(event: MouseEvent): Unit =
    if((event.getScreenX + event.getScreenY)%2 < 1){
      deselectSelectedNodes()
      state = state.copy(selectedNodes = getIntersectingNetworkNodes(selectVolume))
      state.selectedNodes.foreach(_.select())
    }

  private final def getIntersectingNetworkNodes(shape: Shape3D): Set[NetworkNode] =
    getAllNetworkNodes.filter(areNodesIntersecting(shape, _))

  private final def areNodesIntersecting(node1: Node, node2: Node): Boolean =
      node1.getBoundsInParent.intersects(node2.getBoundsInParent)

  protected final def endSelection(event: MouseEvent): Unit =
    onFX {if(selectVolume.isVisible) state = state.copy(selectionComplete = true)}

  protected final def endSelectionMovement(): Unit = onFX {
    mainScene.getChildren.remove(selectVolume)
    selectVolume.setVisible(false)
  }

  protected final def isSelectionComplete: Boolean = state.selectionComplete

  protected final def isUserMovingNodes(event: MouseEvent): Boolean =
    isSelectionComplete && event.getPickResult.getIntersectedNode != null &&
      areNodesIntersecting(selectVolume, event.getPickResult.getIntersectedNode)

  /** Sets the action to execute whenever the user moves the selected nodes.
   * @param action the action to set */
  final def setActionOnMovedNodes(action: Set[(String, Product3[Double, Double, Double])] => Unit): Unit =
    onFX {state = state.copy(movementAction = action)}

  /** Gets the IDs of the currently selected nodes.
   * @return a Set containing the IDs of the currently selected nodes */
  final def getSelectedNodesIDs: Set[String] = onFXAndWait(state.selectedNodes.map(_.UID))

  /** Gets the first node selected, the one that is in the center of the selection cube.
   * @return a String if the initial node exists, None otherwise. */
  final def getInitialSelectedNodeId: Option[String] = onFXAndWait(state.initialNode.map(_.UID))

  /** Sets the color of only the currently selected nodes. This does not set the color of every node that might get
   *  selected in the future.
   * @param color the new color of the selected nodes
   * @return Unit, since it has the side effect of setting the selected nodes' color */
  final def setCurrentSelectionColor(color: java.awt.Color): Unit =
    onFX(state.selectedNodes.foreach(_.setNodeColor(color.toScalaFx)))

  /** This can be used to know whether the user is currently attempting a selection or not.
   * @return true if the user is currently attempting a selection, false otherwise */
  final def isAttemptingSelection: Boolean = onFXAndWait(selectVolume.isVisible)
}
