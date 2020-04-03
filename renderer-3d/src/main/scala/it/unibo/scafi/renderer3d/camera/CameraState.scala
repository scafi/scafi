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
import scalafx.geometry.Point2D

/**
 * This class contains the main state of the camera, as an immutable object.
 * */
private[camera] final case class CameraState(oldMousePosition: Point2D = new Point2D(0, 0),
                                             moveDirections: Set[MoveDirection.Value] = Set(),
                                             rotateDirection: Option[RotateDirection.Value] = None,
                                             onCameraChange: () => Unit = () => ()) {

  /**
   * Creates a new instance that contains one more move direction.
   * @param direction the new direction to be added to the set
   * @return the new CameraState instance
   * */
  def withAddedMoveDirection(direction: MoveDirection.Value): CameraState =
    this.copy(moveDirections = moveDirections + direction)

  /**
   * Creates a new instance that contains one less move direction.
   * @param direction the direction to be removed from the set
   * @return the new CameraState instance
   * */
  def withRemovedMoveDirection(direction: MoveDirection.Value): CameraState =
    this.copy(moveDirections = moveDirections - direction)

}
