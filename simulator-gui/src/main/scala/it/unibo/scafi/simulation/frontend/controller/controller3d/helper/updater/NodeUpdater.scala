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

package it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.frontend.model.Node

/** Interface to update the scene in the view and the simulation from the simulation updates. */
trait NodeUpdater {

  /** Resets the collections that keep information about the nodes. */
  def resetNodeCache(): Unit

  /** Most important method of this class. It updates the specified node in the UI and in the simulation.
   * Most of the calculations are done outside of theJavaFx thread to reduce lag.
   * @param nodeId the id of the node to update */
  def updateNode(nodeId: Int): Unit

  /** Sets the node's color as the color relative to the first enabled sensor in that node.
   * @param node the node that has to be checked
   * @param gui3d the 3D network renderer */
  def updateNodeColorBySensors(node: Node, gui3d: NetworkRenderer3D): Unit

}
