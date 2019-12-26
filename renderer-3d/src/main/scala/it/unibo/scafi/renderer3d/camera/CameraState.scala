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

private[camera] final case class CameraState(oldMousePosition: Point2D = new Point2D(0, 0),
                       moveDirections: Set[MoveDirection.Value] = Set(),
                       rotateDirection: Option[RotateDirection.Value] = None) {

  def withOldMousePosition(value: Point2D): CameraState = this.copy(oldMousePosition = value)

  def withAddedMoveDirection(value: MoveDirection.Value): CameraState =
    this.copy(moveDirections = moveDirections + value)

  def withRemovedMoveDirection(value: MoveDirection.Value): CameraState =
    this.copy(moveDirections = moveDirections - value)

  def withRotateDirection(value: Option[RotateDirection.Value]): CameraState = this.copy(rotateDirection = value)

}
