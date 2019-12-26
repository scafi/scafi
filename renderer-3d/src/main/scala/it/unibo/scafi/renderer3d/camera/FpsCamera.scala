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

import java.util.{Timer, TimerTask}

import it.unibo.scafi.renderer3d.camera.Direction.{MoveDirection, RotateDirection}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.input
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import org.scalafx.extras._
import scalafx.geometry.Point3D
import scalafx.scene.PerspectiveCamera
import scalafx.scene.transform.{Rotate, Translate}

/**
 * JavaFx 3D camera that moves with keyboard input and rotates with mouse or keyboard input.
 * It can move up, down, left, right, forward and backwards but it rotates only on the Y axis.
 * */
final class FpsCamera(initialPosition: Point3D = Point3D.Zero, sensitivity: Double = 0.6d)
  extends PerspectiveCamera(true) with SimulationCamera {

  private[this] val UPDATE_PERIOD_MILLIS = 33 //the camera will be moved and rotated at more or less 30 fps
  private[this] val INITIAL_FOV = 40
  private[this] val MIN_SENSITIVITY = 0.1
  private[this] val MAX_SENSITIVITY = 1d
  private[this] val KEYBOARD_ARROW_SENSITIVITY = 2
  private[this] val MAX_ROTATION = 15
  private[this] val adjustedSensitivity = RichMath.clamp(sensitivity, MIN_SENSITIVITY, MAX_SENSITIVITY)
  @volatile private[this] var state: CameraState = CameraState()

  setup()

  private def setup(): Unit = {
    this.setFieldOfView(INITIAL_FOV)
    this.setFarClip(60000.0)
    this.setNearClip(0.1)
    this.moveTo(initialPosition)
    new Timer().schedule(new TimerTask { //loop to continuously move and rotate the camera
      override def run(): Unit =
        if(state.rotateDirection.isDefined || state.moveDirections.nonEmpty){
          onFX {state.rotateDirection.fold()(rotateByDirection); moveByDirections(state.moveDirections)}
      }
    }, 0, UPDATE_PERIOD_MILLIS)
  }

  /** See [[SimulationCamera.initiateMouseRotation]] */
  override def initiateMouseRotation(mouseEvent: MouseEvent): Unit =
    onFX {state = state.withOldMousePosition(mouseEvent.getScreenPosition)}

  /** See [[SimulationCamera.rotateByMouseEvent]] */
  override def rotateByMouseEvent(mouseEvent: MouseEvent): Unit = onFX {
    val newMousePosition = mouseEvent.getScreenPosition
    this.rotateCamera(RichMath.clamp((newMousePosition.x - state.oldMousePosition.x)/5, -MAX_ROTATION, MAX_ROTATION))
    state = state.withOldMousePosition(newMousePosition)
  }

  private def rotateByDirection(direction: RotateDirection.Value): Unit =
    this.rotateCamera(getKeyboardRotation(direction))

  private def getKeyboardRotation(direction: RotateDirection.Value): Int = direction match {
    case RotateDirection.left => -KEYBOARD_ARROW_SENSITIVITY
    case RotateDirection.right => KEYBOARD_ARROW_SENSITIVITY
  }

  private def rotateCamera(yAxisDegrees: Double): Unit =
    this.rotateOnSelf(adjustedSensitivity * yAxisDegrees, Rotate.YAxis)

  /** See [[SimulationCamera.zoomByKeyboardEvent]] */
  override def zoomByKeyboardEvent(keyEvent: input.KeyEvent): Unit = //doesn't support multiple presses at the same time
    keyEvent.getCode match {
      case KeyCode.ADD => addZoomAmount(1)
      case KeyCode.SUBTRACT => addZoomAmount(-1)
      case _ => ()
    }

  private def addZoomAmount(amount: Int): Unit =
    onFX {this.setFieldOfView(RichMath.clamp(this.getFieldOfView - amount, INITIAL_FOV/2, INITIAL_FOV))}

  private def moveByDirections(directions: Set[MoveDirection.Value]): Unit = state.moveDirections.foreach(moveCamera)

  /** See [[SimulationCamera.initialize()]] */
  override def initialize(): Unit = {
    val scene = this.getScene
    scene.setOnKeyPressed(event => {
      MoveDirection.getDirection(event).fold()(direction => state = state.withAddedMoveDirection(direction))
      RotateDirection.getDirection(event).fold()(direction => state = state.withRotateDirection(Option(direction)))
    })
    scene.setOnKeyReleased(event => {
      MoveDirection.getDirection(event).fold()(direction => state = state.withRemovedMoveDirection(direction))
      RotateDirection.getDirection(event).fold()(_ => state = state.withRotateDirection(None))
    })
  }

  private def moveCamera(cameraDirection: MoveDirection.Value): Unit = {
    val SPEED = 200
    val speedVector = cameraDirection.toVector * SPEED
    this.getTransforms.add(new Translate(speedVector.getX, speedVector.getY, speedVector.getZ))
  }

  /** See [[SimulationCamera.isEventAMovementOrRotation]] */
  override def isEventAMovementOrRotation(keyEvent: KeyEvent): Boolean =
    MoveDirection.getDirection(keyEvent).isDefined || RotateDirection.getDirection(keyEvent).isDefined
}

object FpsCamera {
  def apply(): FpsCamera = new FpsCamera()
  def apply(position: Point3D): FpsCamera = new FpsCamera(position)
  def apply(position: Point3D, sensitivity: Double): FpsCamera = new FpsCamera(position, sensitivity)
}
