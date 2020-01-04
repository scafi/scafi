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

package it.unibo.scafi.renderer3d.manager.selection

import it.unibo.scafi.renderer3d.node.NetworkNode
import scalafx.geometry.Point2D

/**
 * This class contains the main state of SelectionManager, as an immutable object.
 * @param movementTask the task that will move the currently selected nodes in the new position
 * @param movementAction the listener to execute whenever nodes are moved by the user
 * */
private[selection] final case class SelectionManagerState(selectedNodes: Set[NetworkNode] = Set(),
                                                        initialNode: Option[NetworkNode] = None,
                                                        mousePosition: Option[Point2D] = None,
                                                        selectionComplete: Boolean = false,
                                                        movementTask: Option[() => Unit] = None,
                                      movementAction: Set[(String, Product3[Double, Double, Double])] => Unit = _ => ())
