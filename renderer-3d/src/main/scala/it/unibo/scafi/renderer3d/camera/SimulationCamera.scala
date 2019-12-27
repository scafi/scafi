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

import javafx.scene.input.KeyEvent
import scalafx.scene.{Camera, Scene}

/**
 * This trait is an interface of a camera that supports operations like movement and rotation.
 * */
trait SimulationCamera extends Camera{

  /**
   * Lets the camera be used in the scene. Also, it enables the use of more than one pressed key at the same time.
   * @param scene the scene that contains this camera
   * */
  def initialize(scene: Scene): Unit

  /**
   * This can be used to check if a keyboard event is a rotation or a movement.
   * @param keyEvent the keyboard event that will be checked
   * @return true if the event will cause a rotation or movement, false otherwise.
   * */
  def isEventAMovementOrRotation(keyEvent: KeyEvent): Boolean
}
