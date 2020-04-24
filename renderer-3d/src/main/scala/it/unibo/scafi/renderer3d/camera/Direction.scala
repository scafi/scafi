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
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.input.{KeyCode, KeyEvent}

/**
 * It contains enums for movement and rotation directions.
 * */
private[camera] object Direction {

  /** Enum containing the possible directions that the camera can rotate towards. */
  object RotateDirection extends Enumeration {
    val left, right = Value

    def getDirection(keyEvent: KeyEvent): Option[RotateDirection.Value] =
      keyEvent.getCode match {
        case KeyCode.LEFT => Option(RotateDirection.left)
        case KeyCode.RIGHT => Option(RotateDirection.right)
        case _ => None
      }
  }

  /** Enum containing the possible directions that the camera can go towards. */
  object MoveDirection extends Enumeration {
    val forward, backward, left, right, up, down = Value

    private val FORWARD = new Point3D(0, 0, 1)
    private val BACKWARD = FORWARD.negate
    private val LEFT = new Point3D(-1, 0, 0)
    private val RIGHT = LEFT.negate
    private val UP = new Point3D(0, -1, 0)
    private val DOWN = UP.negate

    implicit class CameraMoveDirectionValue(direction: MoveDirection.Value) {
      /**
       * Create a direction vector from the instance of CameraMoveDirection.
       * @return the direction as a Point3D
       * */
      final def toVector: Point3D =
        this.direction match {
          case MoveDirection.forward => FORWARD
          case MoveDirection.backward => BACKWARD
          case MoveDirection.left => LEFT
          case MoveDirection.right => RIGHT
          case MoveDirection.up => UP
          case MoveDirection.down => DOWN
        }
    }

    def getDirection(keyEvent: KeyEvent): Option[MoveDirection.Value] =
      keyEvent.getCode match {
        case KeyCode.W => Option(MoveDirection.forward)
        case KeyCode.S => Option(MoveDirection.backward)
        case KeyCode.A => Option(MoveDirection.left)
        case KeyCode.D => Option(MoveDirection.right)
        case KeyCode.SPACE => Option(MoveDirection.up)
        case KeyCode.C => Option(MoveDirection.down)
        case _ => None
      }
  }
}
