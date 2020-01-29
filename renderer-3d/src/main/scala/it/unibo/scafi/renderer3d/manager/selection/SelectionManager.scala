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

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import it.unibo.scafi.renderer3d.manager.node.NodeManager
import it.unibo.scafi.renderer3d.manager.selection.SelectionManagerHelper._
import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.rendering.Rendering3DUtils._
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.CullFace
import it.unibo.scafi.renderer3d.util.ScalaFxExtras._
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.{Camera, PerspectiveCamera, Scene}

/** Trait that contains some of the main API of the renderer-3d module regarding the nodes' selection. */
private[manager] trait SelectionManager {
  this: NodeManager => //NodeManager has to also be mixed in with SelectionManager

  protected val mainScene: Scene
  private[this] val selectVolume = createCube(1, Color.color(0.2, 0.2, 0.8, 0.5))
  @volatile private[this] var state = SelectionManagerState()
  private[this] val movementExecutor = new ThreadPoolExecutor(1, 1, 1,
    TimeUnit.MINUTES, new LinkedBlockingQueue[Runnable]())

  {selectVolume.setCullFace(CullFace.NONE); selectVolume.setVisible(false)} //setup of selectVolume

  protected final def setSelectionVolumeCenter(event: MouseEvent): Unit = //use minByOption when using Scala 2.13
    onFX (state = state.copy(selectionComplete = false, initialNode = {
      val pickedNode = Option(event.getPickResult.getIntersectedNode)
      val networkNode = pickedNode.flatMap{case node: NetworkNode => Option(node); case node => node.getParent match {
        case networkNode: NetworkNode => Option(networkNode); case _ => None}}
      networkNode.fold(findClosestNodeOnScreen(event, mainScene, getAllNetworkNodes))(Option(_))
    }))

  protected final def setMousePosition(event: MouseEvent, mouseOnSelectionCheck: Boolean = true): Unit =
    onFX {state = state.copy(mousePosition =
      if(!mouseOnSelectionCheck || isMouseOnSelection(event, selectVolume)) Option(event.getScreenPosition) else None)}

  protected final def startSelection(event: MouseEvent): Unit =
    onFX {SelectionManagerHelper.startSelection(event, state, mainScene, selectVolume)}

  private final def deselectSelectedNodes(): Unit =
    {state.selectedNodes.foreach(_.deselect()); state = state.copy(selectedNodes = Set())}

  protected final def moveSelectedNodesIfNeeded(camera: PerspectiveCamera, event: MouseEvent): Unit =
    if(state.mousePosition.isDefined) onFX { //does nothing if the previous task is still running
      state = state.copy(movementTask = Option(() => {
        val movementVector = getMovementVector(event, state, mainScene, camera)
        setMousePosition(event, mouseOnSelectionCheck = false)
        state.movementAction(state.selectedNodes.map(_.UID), movementVector.toProduct) //blocking
        onFXAndWait {
          selectVolume.moveTo(selectVolume.getPosition + movementVector)
          if(state.movementTask.isDefined) submitMovementTaskToExecutor(movementExecutor, state)
          state = state.copy(movementTask = None) //removes the task
        }
      })) //the simulator is then expected to update the nodes positions of the 3d view
      if(movementExecutor.getActiveCount == 0) submitMovementTaskToExecutor(movementExecutor, state)
  }

  protected final def scaleSelectionVolumeIfNeeded(camera: Camera, event: MouseEvent): Unit =
    modifySelectionVolume(camera, event, updateSelectionVolume)

  protected final def changeSelectionVolumeSizesIfNeeded(camera: Camera, event: MouseEvent): Unit =
    modifySelectionVolume(camera, event, changeSelectVolumeSizes)

  protected final def changeSelectionVolumeSizesIfNeeded(camera: Camera, movement: Point2D): Unit =
    {changeSelectVolumeSizes(selectVolume, state, movement, camera); updateSelection()}

  private final def modifySelectionVolume(camera: Camera, event: MouseEvent,
                                          action: (Node, SelectionManagerState, MouseEvent, Camera) => Unit): Unit =
    onFX (if(shouldUpdateNode(event, selectVolume)) {action(selectVolume, state, event, camera); updateSelection()})

  private final def updateSelection(): Unit = {
    deselectSelectedNodes()
    state = state.copy(selectedNodes = getAllNetworkNodes.filter(_.nodeIntersectsWith(selectVolume)))
    state.selectedNodes.foreach(_.select())
  }

  protected final def endSelectionAndRotateSelectedLabels(event: MouseEvent): Unit = onFX (if(selectVolume.isVisible) {
    state = state.copy(selectionComplete = true)
    if(state.mousePosition.isDefined) rotateNodeLabelsIfNeeded(Option(state.selectedNodes))
  })

  protected final def endSelectionMovementIfNeeded(event: Option[MouseEvent]): Unit = onFX (if(isSelectionComplete) {
    event.fold(endSelectionMovement())(event => if(!isMouseOnSelection(event, selectVolume)) endSelectionMovement())
  }) //forces end if None is provided

  private def endSelectionMovement(): Unit = {
    hideSelectVolume(mainScene, selectVolume)
    state = state.copy(mousePosition = None)
    deselectSelectedNodes()
  }

  protected final def isSelectionComplete: Boolean = state.selectionComplete

  protected final def movementComplete: Boolean = !selectVolume.isVisible

  /** Sets the action to execute whenever the user moves the selected nodes.
   * @param action the action to set */
  final def setActionOnMovedNodes(action: (Set[Int], Product3[Double, Double, Double]) => Unit): Unit =
    onFX {state = state.copy(movementAction = action)}

  /** Gets the IDs of the currently selected nodes.
   * @return a Set containing the IDs of the currently selected nodes */
  final def getSelectedNodesIDs: Set[Int] = onFXAndWait(state.selectedNodes.map(_.UID))

  /** Gets the first node selected, the one that is in the center of the selection cube.
   * @return a String if the initial node exists, None otherwise. */
  final def getInitialSelectedNodeId: Option[Int] = onFXAndWait(state.initialNode.map(_.UID))

  /** Sets the color of the selected nodes. This does not set the color of nodes that might get selected in the future.
   * @param color the new color of the selected nodes */
  final def setCurrentSelectionColor(color: java.awt.Color): Unit =
    onFX(state.selectedNodes.foreach(_.setNodeColor(color.toScalaFx)))

  /** This can be used to know whether the user is currently attempting a selection or not.
   * @return true if the user is currently attempting a selection, false otherwise */
  final def isAttemptingSelection: Boolean = onFXAndWait(selectVolume.isVisible)

  /** Sets the color of the current selection.
   * @param color the new color of the selection */
  final def setSelectionColor(color: java.awt.Color): Unit = onFX {
    val MIN_ALPHA = 40 //otherwise it's not visible
    selectVolume.setColor(new java.awt.Color(color.getRed, color.getGreen, color.getBlue,
      if(color.getAlpha == 0) 0 else Math.max(color.getAlpha, MIN_ALPHA)))
  }
}
