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

package it.unibo.scafi.renderer3d.manager.node

import java.awt.Color

import it.unibo.scafi.renderer3d.manager.node.NodeManagerState.NODE_BRIGHTNESS
import it.unibo.scafi.renderer3d.node.NetworkNode
import scalafx.geometry.Point3D

/**
 * This class contains the main state of NodeManager, as an immutable object.
 * */
private[node] final case class NodeManagerState(nodesColor: Color =
                                                   new Color(NODE_BRIGHTNESS, NODE_BRIGHTNESS, NODE_BRIGHTNESS),
                                  movementColor: Color = Color.RED,
                                  filledSpheresColor: Color = java.awt.Color.yellow,
                                  positionThatLabelsFace: Point3D = Point3D.Zero,
                                  seeThroughSpheresRadius: Double = 0,
                                  filledSpheresRadius: Double = 0,
                                  nodesScale: Double = 1,
                                  nodeLabelsScale: Double = 1,
                                  networkNodes: Map[String, NetworkNode] = Map())

object NodeManagerState {
  private final val NODE_BRIGHTNESS = 50 //out of 255
}
