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

import scalafx.geometry.Point3D
import scalafx.scene.Camera
import it.unibo.scafi.renderer3d.util.RichScalaFx._

import scala.language.implicitConversions

/**
 * This enum is a type containing all the possible directions that the camera can go towards.
 * */
private[camera] object CameraMoveDirection extends Enumeration {
  val forward, backward, left, right, up, down = Value

  private val FORWARD = new Point3D(0, 0, 1)
  private val BACKWARD = FORWARD.negate
  private val LEFT = new Point3D(-1, 0, 0)
  private val RIGHT = LEFT.negate
  private val UP = new Point3D(0, -1, 0)
  private val DOWN = UP.negate

  implicit class CameraMoveDirectionValue(direction: CameraMoveDirection.Value) {
    /**
     * Create a direction vector from the instance of CameraMoveDirection.
     * @return the direction as a Point3D
     * */
    final def toVector: Point3D =
      this.direction match {
        case CameraMoveDirection.forward => FORWARD
        case CameraMoveDirection.backward => BACKWARD
        case CameraMoveDirection.left => LEFT
        case CameraMoveDirection.right => RIGHT
        case CameraMoveDirection.up => UP
        case CameraMoveDirection.down => DOWN
      }
  }
}
