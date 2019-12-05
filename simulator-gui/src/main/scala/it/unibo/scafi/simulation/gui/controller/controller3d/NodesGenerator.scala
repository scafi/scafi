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

package it.unibo.scafi.simulation.gui.controller.controller3d

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.simulation.gui.Settings
import it.unibo.scafi.simulation.gui.SettingsSpace.Topologies.{Grid, Grid_HighVar, Grid_LoVar, Grid_MedVar}
import it.unibo.scafi.simulation.gui.model.Node
import it.unibo.scafi.simulation.gui.model.implementation.NodeImpl
import it.unibo.scafi.space.{Point3D, SpaceHelper}

import scala.util.Random

private[controller3d] object NodesGenerator {

  def createNodes(topology: String, nodeCount: Int, seed: Long): Map[Int, Node] = {
    if(topology.contains("grid")){
      val nodeCountInSide = Math.cbrt(nodeCount).toInt
      val step = 1.0 / nodeCountInSide
      val OFFSET = 0.05
      val variance = getVariance(topology)
      val gridSettings = GridSettings(nodeCountInSide, nodeCountInSide, step , step, variance, OFFSET, OFFSET).to3D
      val locations =  SpaceHelper.grid3DLocations(gridSettings, seed)
      locations.zipWithIndex.toMap.map(_.swap).map({case (index, position) => (index, new NodeImpl(index, position))})
    } else {
      (1 to nodeCount).map(index => (index, new NodeImpl(index, getRandomPosition))).toMap
    }
  }

  private def getVariance(topology: String): Double = topology match { //TODO: remove copy-paste
    case Grid => 0
    case Grid_LoVar => Settings.Grid_LoVar_Eps
    case Grid_MedVar => Settings.Grid_MedVar_Eps
    case Grid_HighVar => Settings.Grid_HiVar_Eps
  }

  private def getRandomPosition: Point3D = {
    val MAX_DISTANCE = 10000
    new Point3D(randomDouble(MAX_DISTANCE), randomDouble(MAX_DISTANCE), randomDouble(MAX_DISTANCE))
  }

  private def randomDouble(maxValue: Int): Double = Random.nextInt(maxValue).toDouble

}
