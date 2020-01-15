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

package it.unibo.scafi.renderer3d.camera

import it.unibo.scafi.renderer3d.camera.CameraHelper._
import it.unibo.scafi.renderer3d.camera.Direction.{MoveDirection, RotateDirection}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import it.unibo.scafi.renderer3d.util.ScalaFxExtras._
import javafx.scene.input
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import org.fxyz3d.geometry.MathUtils
import scalafx.animation.AnimationTimer
import scalafx.geometry.Point3D
import scalafx.scene.transform.Rotate
import scalafx.scene.{PerspectiveCamera, Scene}

/** JavaFx 3D camera that moves with keyboard input and rotates with mouse or keyboard input.
 * It can move up, down, left, right, forward and backwards but it rotates only on the Y axis. */
final class FpsCamera(initialPosition: Point3D = Point3D.Zero, sensitivity: Double = 0.6d)
  extends PerspectiveCamera(true) with SimulationCamera {

  private[this] val INITIAL_FOV = 40
  private[this] val MIN_SENSITIVITY = 0.1
  private[this] val MAX_SENSITIVITY = 1d
  private[this] val MAX_ROTATION = 15
  private[this] val adjustedSensitivity = MathUtils.clamp(sensitivity, MIN_SENSITIVITY, MAX_SENSITIVITY)
  private[this] var state: CameraState = CameraState()

  setupCamera(this, INITIAL_FOV, initialPosition)
  setupMovementAndRotations()

  private def setupMovementAndRotations(): Unit = {
    var previousTime = System.nanoTime()
    AnimationTimer(time => {
      val delay = (time - previousTime)/10000000
      if(state.rotateDirection.isDefined || state.moveDirections.nonEmpty) state.onCameraChange()
      state.rotateDirection.fold()(direction => rotateCamera(getKeyboardRotation(direction) * delay))
      moveByDirections(this, state.moveDirections, delay)
      previousTime = time
    }).start()
  }

  private def rotateCamera(yAxisDegrees: Double): Unit =
    this.rotateOnSelf(adjustedSensitivity * yAxisDegrees, Rotate.YAxis)

  /** See [[SimulationCamera.startMouseRotation()]] */
  override def startMouseRotation(mouseEvent: MouseEvent): Unit =
    onFX {state = state.copy(oldMousePosition = mouseEvent.getScreenPosition)}

  /** See [[SimulationCamera.rotateByMouseEvent()]] */
  override def rotateByMouseEvent(mouseEvent: MouseEvent): Unit = onFX {
    val newMousePosition = mouseEvent.getScreenPosition
    rotateCamera(MathUtils.clamp((newMousePosition.x - state.oldMousePosition.x)/5, -MAX_ROTATION, MAX_ROTATION))
    state = state.copy(oldMousePosition = newMousePosition)
  }

  private def zoomByKeyboardEvent(keyEvent: input.KeyEvent): Unit = {
    val zoomStep = 0.75
    keyEvent.getCode match {
      case KeyCode.ADD => addZoomAmount(zoomStep)
      case KeyCode.SUBTRACT => addZoomAmount(-zoomStep)
      case _ => ()
    }
  }

  private def addZoomAmount(amount: Double): Unit =
    onFX {this.setFieldOfView(MathUtils.clamp(this.getFieldOfView - amount, INITIAL_FOV/2, INITIAL_FOV))}

  /** See [[SimulationCamera.initialize]] */
  override def initialize(scene: Scene, onCameraChangeAction: () => Unit): Unit = onFX {
    scene.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => {
      MoveDirection.getDirection(event).fold()(direction => state = state.withAddedMoveDirection(direction))
      RotateDirection.getDirection(event).fold()(direction => state = state.copy(rotateDirection = Option(direction)))
      zoomByKeyboardEvent(event)
    })
    scene.addEventFilter(KeyEvent.KEY_RELEASED, (event: KeyEvent) => {
      MoveDirection.getDirection(event).fold()(direction => state = state.withRemovedMoveDirection(direction))
      RotateDirection.getDirection(event).fold()(direction =>
        if (state.rotateDirection == Option(direction)) state = state.copy(rotateDirection = None))
    })
    state = state.copy(onCameraChange = onCameraChangeAction)
  }

  /** See [[SimulationCamera.stopMovingAndRotating]] */
  override def stopMovingAndRotating(): Unit = onFX {state = state.copy(moveDirections = Set(), rotateDirection = None)}

  /** See [[SimulationCamera.isEventAMovementOrRotation]] */
  override def isEventAMovementOrRotation(keyEvent: KeyEvent): Boolean =
    MoveDirection.getDirection(keyEvent).isDefined || RotateDirection.getDirection(keyEvent).isDefined
}

object FpsCamera {
  def apply(): FpsCamera = new FpsCamera()
  def apply(position: Point3D): FpsCamera = new FpsCamera(position)
  def apply(position: Point3D, sensitivity: Double): FpsCamera = new FpsCamera(position, sensitivity)
}
