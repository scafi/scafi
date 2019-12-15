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

import java.awt.Color
import it.unibo.scafi.renderer3d.camera.{FpsCamera, SimulationCamera}
import javafx.scene.input
import javafx.scene.input.{MouseButton, MouseEvent}
import org.scalafx.extras.onFX
import scalafx.geometry.Point3D
import scalafx.scene.{Group, Scene, SceneAntialiasing}
import scalafx.scene.input.KeyEvent
import it.unibo.scafi.renderer3d.util.Rendering3DUtils
import it.unibo.scafi.renderer3d.util.RichScalaFx._

/**
 *  Trait that contains some of the main API of the renderer-3d module regarding the scene: the method that resets
 *  the scene, etc.
 *  */
private[manager] trait SceneManager {
  this: NodeManager with SelectionManager => //NodeManager has to also be mixed in with SceneManager

  protected val mainScene: Scene

  /** Sets the background color of the scene.
   * @param color the color to set
   * @return Unit, since it has the side effect of setting the scene's color
   * */
  def setBackgroundColor(color: Color): Unit = onFX {mainScene.setFill(color.toScalaFx)}

  /** Resets the scene: this means deleting all the nodes and connections, obtaining an empty scene (the light and
   *  camera will still exist, though)
   * @return Unit, since it has the side effect of resetting the scene
   * */
  def resetScene(): Unit = onFX {
    getAllNetworkNodes.foreach(node => removeNode(node.UID))
    mainScene.getCamera.moveTo(Point3D.Zero)
    mainScene.getCamera.lookAtOnXZPlane(new Point3D(1, 0, 0))
  }

  protected def createScene(): Scene = {
    new Scene(0, 0, true, SceneAntialiasing.Balanced) {
      val simulationCamera: SimulationCamera = FpsCamera()
      camera = simulationCamera
      root = new Group(simulationCamera, Rendering3DUtils.createAmbientLight)
      setKeyboardInteraction(this, simulationCamera)
      setMouseInteraction(this, simulationCamera)
    }
  }

  private[this] def setKeyboardInteraction(scene: Scene, camera: SimulationCamera): Unit =
    scene.addEventFilter(KeyEvent.KeyPressed, (event: input.KeyEvent) => {
      if (camera.isEventAMovementOrRotation(event)) rotateNodeLabelsIfNeeded(camera)
      camera.moveByKeyboardEvent(event)
      camera.zoomByKeyboardEvent(event)
      camera.rotateByKeyboardEvent(event)
    })

  private[this] def setMouseInteraction(scene: Scene, camera: SimulationCamera): Unit = {
    scene.setOnDragDetected(_ => scene.startFullDrag())
    scene.setOnMousePressed(onMousePressed(_, camera))
    scene.onMouseDragEntered = event => if(isPrimaryButton(event)) startSelection(event)
    scene.onMouseDragged = event => {
      if(isPrimaryButton(event)) modifySelectionVolume(camera, event)
      if(event.getButton == MouseButton.MIDDLE) camera.rotateByMouseEvent(event)
    }
    scene.onMouseReleased = event => if(isPrimaryButton(event)) endSelection(event)
  }

  private[this] def onMousePressed(event: MouseEvent, camera: SimulationCamera): Unit = {
    if(isPrimaryButton(event)){
      setSelectionVolumeCenter(event)
    } else if(isMiddleMouse(event)){
      camera.initiateMouseRotation(event)
    }
  }

  private[this] def isPrimaryButton(event: MouseEvent): Boolean = event.getButton == MouseButton.PRIMARY

  private[this] def isMiddleMouse(event: MouseEvent): Boolean = event.getButton == MouseButton.MIDDLE

}
