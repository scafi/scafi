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

import it.unibo.scafi.space.{Point2D, Point3D}

/**
 * This trait is an interface of a camera that supports operations to move and turn the camera by mouse and keyboard.
 * Each method takes as an input a key or mouse event and if the correct button is used then the specified action
 * gets executed.
 * */
trait SimulationCamera extends Camera{

  /**
   * Lets the camera be used in the scene. Also, it enables the use of more than one pressed key at the same time.
   * */
  def initialize(): Unit

  /**
   * This has to be called before actually rotating the camera; it indicates the start of the rotation.
   * @param mouseEvent the mouse event that starts the rotation
   * @return Unit, since it has the side effect of preparing the camera for rotation
   * */
  def initiateMouseRotation(mouseEvent: MouseEvent): Unit

  /**
   * Rotates the camera based on the new position of the mouse.
   * @param mouseEvent the mouse event that will be used to rotate the camera
   * @return Unit, since it has the side effect of rotating the camera
   * */
  def rotateByMouseEvent(mouseEvent: MouseEvent): Unit

  /**
   * Zooms the camera based on the pressed keyboard key.
   * @param keyEvent the keyboard event that will be used to zoom the camera
   * @return Unit, since it has the side effect of zooming the camera
   * */
  def zoomByKeyboardEvent(keyEvent: KeyEvent): Unit

  /**
   * This can be used to check if a keyboard event is a rotation or a movement.
   * @param keyEvent the keyboard event that will be checked
   * @return true if the event will cause a rotation or movement, false otherwise.
   * */
  def isEventAMovementOrRotation(keyEvent: KeyEvent): Boolean
}
