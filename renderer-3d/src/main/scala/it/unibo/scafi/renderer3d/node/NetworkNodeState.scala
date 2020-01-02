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

package it.unibo.scafi.renderer3d.node

import scalafx.geometry.Point3D
import scalafx.scene.paint.Color

/**
 * This class contains the main state of SimpleNetworkNode, as an immutable object.
 * */
private[node] final case class NetworkNodeState(currentColor: Color = Color.Black,
                            scale: Double = 1d,
                            selectionColor: Color = Color.Red,
                            currentPosition: Point3D = Point3D.Zero)

object NetworkNodeState {
  def apply(currentColor: Color, currentPosition: Point3D): NetworkNodeState =
    new NetworkNodeState(currentColor, 1, Color.Red, currentPosition)
}
