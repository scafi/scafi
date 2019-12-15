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

import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.input
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import org.scalafx.extras._
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.PerspectiveCamera
import scalafx.scene.transform.{Rotate, Translate}

import scala.collection.mutable.{Set => MutableSet}

/**
 * JavaFx 3D camera that moves with keyboard input and rotates with mouse or keyboard input.
 * It can move up, down, left, right, forward and backwards but it rotates only on the Y axis.
 * */
final class FpsCamera(initialPosition: Point3D = Point3D.Zero, sensitivity: Double = 0.5d)
  extends PerspectiveCamera(true) with SimulationCamera {

  private[this] val INITIAL_FOV = 40
  private[this] val MIN_FOV = 20
  private[this] val MIN_SENSITIVITY = 0.1
  private[this] val MAX_SENSITIVITY = 1d
  private[this] val KEYBOARD_SENSITIVITY = 2
  private[this] val MAX_ROTATION = 15
  private[this] val adjustedSensitivity = RichMath.clamp(sensitivity, MIN_SENSITIVITY, MAX_SENSITIVITY)
  private[this] var oldMousePosition = new Point2D(0, 0)
  private[this] val moveDirections = MutableSet[CameraMoveDirection.Value]()
  private[this] var multipleKeyPressesEnabled = false

  this.setFieldOfView(INITIAL_FOV)
  this.setFarClip(60000.0)
  this.setNearClip(0.1)
  this.moveTo(initialPosition)

  /** See [[SimulationCamera.initiateMouseRotation]] */
  override def initiateMouseRotation(mouseEvent: MouseEvent): Unit = oldMousePosition = mouseEvent.getScreenPosition

  /** See [[SimulationCamera.rotateByMouseEvent]] */
  override def rotateByMouseEvent(mouseEvent: MouseEvent): Unit = onFX {
    val newMousePosition = mouseEvent.getScreenPosition
    this.rotateCamera(RichMath.clamp((newMousePosition.x - oldMousePosition.x)/5, -MAX_ROTATION, MAX_ROTATION))
    oldMousePosition = newMousePosition
  }

  /** See [[SimulationCamera.rotateByKeyboardEvent]] */
  override def rotateByKeyboardEvent(keyEvent: input.KeyEvent): Unit =
    getKeyboardRotation(keyEvent).fold()(this.rotateCamera(_))

  private def getKeyboardRotation(keyEvent: input.KeyEvent): Option[Int] = keyEvent.getCode match {
    case KeyCode.LEFT => Option(-KEYBOARD_SENSITIVITY)
    case KeyCode.RIGHT => Option(KEYBOARD_SENSITIVITY)
    case _ => None
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
    onFX {this.setFieldOfView(RichMath.clamp(this.getFieldOfView - amount, MIN_FOV, INITIAL_FOV))}

  /** See [[SimulationCamera.moveByKeyboardEvent]] */
  override def moveByKeyboardEvent(event: KeyEvent): Unit = onFX {
    if(!multipleKeyPressesEnabled){
      enableMultipleKeyPresses()
    }
    if(moveDirections.isEmpty){
      getMoveDirection(event).fold()(moveCamera)
    } else {
      moveDirections.foreach(moveCamera)
    }
  }

  private def enableMultipleKeyPresses(): Unit = {
    this.getScene.setOnKeyPressed(getMoveDirection(_).fold()(moveDirections.add))
    this.getScene.setOnKeyReleased(getMoveDirection(_).fold()(moveDirections.remove))
    multipleKeyPressesEnabled = true
  }

  private def getMoveDirection(keyEvent: KeyEvent): Option[CameraMoveDirection.Value] =
    keyEvent.getCode match {
      case KeyCode.W => Option(CameraMoveDirection.forward)
      case KeyCode.S => Option(CameraMoveDirection.backward)
      case KeyCode.A => Option(CameraMoveDirection.left)
      case KeyCode.D => Option(CameraMoveDirection.right)
      case KeyCode.SPACE => Option(CameraMoveDirection.up)
      case KeyCode.C => Option(CameraMoveDirection.down)
      case _ => None
    }

  private def moveCamera(cameraDirection: CameraMoveDirection.Value): Unit = {
    val SPEED = 200
    val speedVector = cameraDirection.toVector * SPEED
    this.getTransforms.add(new Translate(speedVector.getX, speedVector.getY, speedVector.getZ))
  }

  /** See [[SimulationCamera.isEventAMovementOrRotation]] */
  override def isEventAMovementOrRotation(keyEvent: KeyEvent): Boolean =
    getMoveDirection(keyEvent).isDefined || getKeyboardRotation(keyEvent).isDefined
}

object FpsCamera {
  def apply(): FpsCamera = new FpsCamera()
  def apply(position: Point3D): FpsCamera = new FpsCamera(position)
  def apply(position: Point3D, sensitivity: Double): FpsCamera = new FpsCamera(position, sensitivity)
}
