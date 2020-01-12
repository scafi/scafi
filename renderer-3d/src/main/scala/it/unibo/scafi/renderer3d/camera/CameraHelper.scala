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

import it.unibo.scafi.renderer3d.camera.Direction.{MoveDirection, RotateDirection}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import scalafx.geometry.Point3D
import scalafx.scene.PerspectiveCamera
import scalafx.scene.transform.Translate

/** Helper object for [[FpsCamera]] with various utility methods. */
private[camera] object CameraHelper {

  private[this] val KEYBOARD_ARROW_SENSITIVITY = 1

  /** Calculates the amount of rotation defined by the user key press.
   *  @param direction the rotation direction
   *  @return the amount of rotation */
  def getKeyboardRotation(direction: RotateDirection.Value): Int = direction match {
    case RotateDirection.left => -KEYBOARD_ARROW_SENSITIVITY
    case RotateDirection.right => KEYBOARD_ARROW_SENSITIVITY
  }

  /** Sets up the camera position, FOV, etc.
   * @param camera the PerspectiveCamera camera to set up
   * @param FOV the FOV to set
   * @param position the 3D position to set */
  def setupCamera(camera: PerspectiveCamera, FOV: Int, position: Point3D): Unit = {
    camera.setFieldOfView(FOV)
    camera.setFarClip(100000.0)
    camera.setNearClip(0.1)
    camera.moveTo(position)
  }

  /** Moves the camera by looking at the provided directions. It makes a bigger movement as the delay between this
   *  frame and the previous one gets bigger.
   * @param directions the movement directions to apply
   * @param delay the delay between this frame and the previous one */
  def moveByDirections(camera: SimulationCamera, directions: Set[MoveDirection.Value], delay: Double): Unit =
    if(directions.nonEmpty){
      val adjustedDelay = delay/Math.sqrt(directions.size)
      directions.foreach(direction => moveCamera(direction, adjustedDelay))
    }

  private def moveCamera(camera: SimulationCamera, cameraDirection: MoveDirection.Value, delay: Double): Unit = {
    val SPEED = 100
    val speedVector = cameraDirection.toVector * SPEED * delay
    val translate = new Translate(speedVector.getX, speedVector.getY, speedVector.getZ)
    camera.getTransforms.add(translate)
    val newPosition = camera.getPosition
    camera.getTransforms.remove(translate) //this is needed, otherwise this.getTransforms grows bigger and bigger
    camera.moveTo(newPosition)
  }

}
