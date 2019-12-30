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

import it.unibo.scafi.renderer3d.manager.SelectionManagerHelper._
import it.unibo.scafi.renderer3d.util.Rendering3DUtils._
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import it.unibo.scafi.renderer3d.util.{FastMath, RunOnExecutor}
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import org.scalafx.extras._
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.paint.Color
import scalafx.scene.transform.Rotate
import scalafx.scene.{PerspectiveCamera, Scene}

/** Trait that contains some of the main API of the renderer-3d module regarding the nodes' selection. */
private[manager] trait SelectionManager {
  this: NodeManager => //NodeManager has to also be mixed in with SelectionManager

  protected val mainScene: Scene
  private[this] val selectVolume = createCube(1, Color.color(0.2, 0.2, 0.8, 0.5))
  private[this] var state = SelectionManagerState()

  setupSelectVolume(selectVolume)

  protected final def setSelectionVolumeCenter(event: MouseEvent): Unit = onFX {
    val screenPosition = event.getScreenPosition
    val allNetworkNodes = getAllNetworkNodes
    if(allNetworkNodes.isEmpty){
      state = state.copy(initialNode = None)
    } else {
      state = state.copy(initialNode = Option(allNetworkNodes.minBy(_.getScreenPosition.distance(screenPosition))))
    }
    state = state.copy(selectionComplete = false)
    deselectSelectedNodes()
  }

  protected final def setMouseSelectingPosition(event: MouseEvent): Unit = onFX {
    val condition = state.mousePosition.nonEmpty || isMouseOnSelection(event, selectVolume)
    state = state.copy(mousePosition = if(condition) Option(event.getScreenPosition) else None)
  }

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

  protected final def moveSelectedNodesIfNeeded(camera: PerspectiveCamera, event: MouseEvent): Unit = onFX {
    val mousePosition = event.getScreenPosition
    if((mousePosition distance state.mousePosition.getOrElse(mousePosition)) > 50){
      val cameraRight = FastMath.rotateVector(Rotate.XAxis, Rotate.YAxis, (-camera.getYRotationAngle - 90).toRadians)
      val mouseMovement = mousePosition subtract state.mousePosition.getOrElse(Point2D.Zero).delegate
      val multiplier = getMovementMultiplier(new Point2D(mouseMovement), camera, state.initialNode, mainScene)
      val movementVector = (cameraRight * multiplier.x) + Rotate.YAxis*multiplier.y
      state.selectedNodes.foreach(node => moveNode(node.UID, (node.getNodePosition + movementVector).toProduct))
      setMouseSelectingPosition(event)
      selectVolume.moveTo(selectVolume.getPosition + movementVector)
      RunOnExecutor {state.movementAction(state.selectedNodes.map(node => (node.UID, node.getNodePosition.toProduct)))}
    }}

  protected final def modifySelectionVolume(camera: Node, event: MouseEvent): Unit = onFX {
    if(selectVolume.isVisible){
      val initialNodeScreenPosition = state.initialNode.map(_.getScreenPosition).getOrElse(Point2D.Zero)
      val cameraToNodeDistance = state.initialNode.map(_.getPosition).getOrElse(Point3D.Zero).distance(camera.getPosition)
      selectVolume.setScale(event.getScreenPosition.distance(initialNodeScreenPosition) * cameraToNodeDistance / 1000)
      selectVolume.lookAtOnXZPlane(camera.getPosition)
      updateSelectionIfNeeded(event)
    }
  }

  private final def updateSelectionIfNeeded(event: MouseEvent): Unit =
    if((event.getScreenX + event.getScreenY)%2 < 1){
      deselectSelectedNodes()
      state = state.copy(selectedNodes = getIntersectingNetworkNodes(selectVolume, getAllNetworkNodes))
      state.selectedNodes.foreach(_.select())
    }

  protected final def endSelection(event: MouseEvent): Unit =
    onFX {if(selectVolume.isVisible) state = state.copy(selectionComplete = true)}

  protected final def endSelectionMovementIfNeeded(event: MouseEvent): Unit = onFX {
    if(isSelectionComplete && !isMouseOnSelection(event, selectVolume)){
      mainScene.getChildren.remove(selectVolume)
      selectVolume.setVisible(false)
      state = state.copy(mousePosition = None)
    }
  }

  protected final def isSelectionComplete: Boolean = state.selectionComplete

  protected final def movementComplete: Boolean = !selectVolume.isVisible

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
