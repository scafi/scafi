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

package it.unibo.scafi.simulation.gui.controller.controller3d.helper

import it.unibo.scafi.config.{GridSettings, SimpleRandomSettings}
import it.unibo.scafi.simulation.gui.controller.ControllerUtils
import it.unibo.scafi.simulation.gui.model.Node
import it.unibo.scafi.simulation.gui.model.implementation.NodeImpl
import it.unibo.scafi.space.SpaceHelper

private[controller3d] object NodesGenerator {

  private val SCENE_SIZE = 10000

  def createNodes(topology: String, nodeCount: Int, seed: Long): Map[Int, Node] = {
    val locations = if(topology.contains("grid")){
      val nodeCountInSide = Math.cbrt(nodeCount).toInt
      val step = SCENE_SIZE / nodeCountInSide
      val OFFSET = SCENE_SIZE/40
      val variance = ControllerUtils.getTolerance(topology) * (SCENE_SIZE/10)
      val gridSettings = GridSettings(nodeCountInSide, nodeCountInSide, step , step, variance, OFFSET, OFFSET).to3D
      SpaceHelper.grid3DLocations(gridSettings, seed)
    } else {
      SpaceHelper.random3DLocations(SimpleRandomSettings(-SCENE_SIZE/2, SCENE_SIZE/2), nodeCount, seed)
    }
    locations.zipWithIndex.toMap.map(_.swap).map({case (index, position) => (index, new NodeImpl(index, position))})
  }

}
