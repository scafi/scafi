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

import javafx.scene.input.{KeyEvent, MouseEvent}
import scalafx.scene.{Camera, Scene}

/**
 * This trait is an interface of a camera that supports operations like movement and rotation.
 * */
trait SimulationCamera extends Camera{

  /**
   * Lets the camera be used in the scene. Also, it enables the use of more than one pressed key at the same time.
   * Be sure that the provided parameters are not null.
   * @param scene the scene that contains this camera
   * @param onCameraChangeAction the action to execute whenever the camera moves or rotates
   * @return Unit, since it has the side effect of initialize the camera
   * */
  def initialize(scene: Scene, onCameraChangeAction: () => Unit): Unit

  /**
   * This can be used to check if a keyboard event is a rotation or a movement.
   * @param keyEvent the keyboard event that will be checked
   * @return true if the event will cause a rotation or movement, false otherwise.
   * */
  def isEventAMovementOrRotation(keyEvent: KeyEvent): Boolean

  /**
   * This has to be called before actually rotating the camera; it indicates the start of the rotation.
   * @param mouseEvent the mouse event that starts the rotation
   * @return Unit, since it has the side effect of preparing the camera for rotation
   * */
  def startMouseRotation(mouseEvent: MouseEvent): Unit

  /**
   * Rotates the camera based on the new position of the mouse.
   * @param mouseEvent the mouse event that will be used to rotate the camera
   * @return Unit, since it has the side effect of rotating the camera
   * */
  def rotateByMouseEvent(mouseEvent: MouseEvent): Unit

  /**
   * Stops any movement or rotation currently active on the camera. This is useful since the camera remembers any pressed
   * key, so if the user stops pressing a movement or rotation key when the main window is not focused, the camera will
   * continue to move or rotate forever. This method should then be called to stop that.
   * @return Unit, since it has the side effect of  stopping the camera from moving and rotating
   * */
  def stopMovingAndRotating(): Unit
}
