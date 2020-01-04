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

import java.awt.event.ActionEvent

import it.unibo.scafi.renderer3d.manager.node.NodeManager
import it.unibo.scafi.renderer3d.manager.selection.SelectionManagerHelper._
import it.unibo.scafi.renderer3d.util.Rendering3DUtils._
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javax.swing.Timer
import org.scalafx.extras._
import scalafx.scene.paint.Color
import scalafx.scene.{Camera, PerspectiveCamera, Scene}

/** Trait that contains some of the main API of the renderer-3d module regarding the nodes' selection. */
private[manager] trait SelectionManager {
  this: NodeManager => //NodeManager has to also be mixed in with SelectionManager

  protected val mainScene: Scene
  private[this] val selectVolume = createCube(1, Color.color(0.2, 0.2, 0.8, 0.5))
  private[this] var state = SelectionManagerState()
  private[this] val timer: Timer =  new Timer(0, (_: ActionEvent) => { //avoids performance issues
    state.movementTask.getOrElse(() => Unit)(); //tells simulation about the movement, blocking, so it's outside javafx
  }) //TODO: don't use javax.swing.Timer, so that the AWT thread does not get blocked

  setupSelectVolume(selectVolume)

  protected final def setSelectionVolumeCenter(event: MouseEvent): Unit = onFX { //use minByOption when using Scala 2.13
    val screenPosition = event.getScreenPosition
    state = state.copy(selectionComplete = false, initialNode = {
      val camera = mainScene.getCamera match {case camera: javafx.scene.PerspectiveCamera => camera}
      val filteredNodes = getAllNetworkNodes.filter(camera.isNodeVisible(_, useSmallerFOVWindow = true))
      if(filteredNodes.isEmpty) None else Option(filteredNodes.minBy(_.getScreenPosition.distance(screenPosition)))
    })
  }

  protected final def setMousePosition(event: MouseEvent, mouseOnSelectionCheck: Boolean = true): Unit =
    onFX {state = state.copy(mousePosition =
      if(!mouseOnSelectionCheck || isMouseOnSelection(event, selectVolume)) Option(event.getScreenPosition) else None)}

  protected final def startSelection(event: MouseEvent): Unit = onFX (
    if(!isMouseOnSelection(event, selectVolume)) {
      SelectionManagerHelper.startSelection(event, state, mainScene, selectVolume)
    })

  private final def deselectSelectedNodes(): Unit =
    {state.selectedNodes.foreach(_.deselect()); state = state.copy(selectedNodes = Set())}

  protected final def moveSelectedNodesIfNeeded(camera: PerspectiveCamera, event: MouseEvent): Unit =
    if(state.mousePosition.isDefined && !timer.isRunning) onFX { //does nothing if the previous task is still running
      state = state.copy(movementTask = Option(() => { //TODO: check thread safety and that javaFx is not being flooded
        val movementVector = getMovementVector(event, state, mainScene, camera)
        setMousePosition(event, mouseOnSelectionCheck = false)
        selectVolume.moveTo(selectVolume.getPosition + movementVector)
        state.movementAction(state.selectedNodes.map(node => (node.UID, movementVector.toProduct))) //blocking
        onFXAndWait {state = state.copy(movementTask = None); timer.stop()} //removes the task
      })) //the simulator is then expected to update the nodes positions in renderer3d
      timer.start() //using a timer instead of an executor since it seems faster
  }

  protected final def scaleSelectionVolumeIfNeeded(camera: Camera, event: MouseEvent): Unit =
    modifySelectionVolume(camera, event, updateSelectionVolume)

  protected final def changeSelectionVolumeSizesIfNeeded(camera: Camera, event: MouseEvent): Unit =
    modifySelectionVolume(camera, event, changeSelectVolumeSizes)

  private final def modifySelectionVolume(camera: Camera, event: MouseEvent,
                                          action: (Node, SelectionManagerState, MouseEvent, Camera) => Unit): Unit =
    onFX (if(shouldUpdateNode(event, selectVolume)) {action(selectVolume, state, event, camera); updateSelection(event)})

  private final def updateSelection(event: MouseEvent): Unit = {
    deselectSelectedNodes()
    state = state.copy(selectedNodes = getIntersectingNetworkNodes(selectVolume, getAllNetworkNodes))
    state.selectedNodes.foreach(_.select())
  }

  protected final def endSelection(event: MouseEvent): Unit =
    onFX {if(selectVolume.isVisible) state = state.copy(selectionComplete = true)}

  protected final def endSelectionMovementIfNeeded(event: Option[MouseEvent]): Unit = //forces end if None is provided
    onFX (if(isSelectionComplete) {
      event.fold(endSelectionMovement())(event => if(!isMouseOnSelection(event, selectVolume)) endSelectionMovement())
    })

  private def endSelectionMovement(): Unit = {
    mainScene.getChildren.remove(selectVolume)
    selectVolume.setVisible(false)
    state = state.copy(mousePosition = None)
    deselectSelectedNodes()
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
