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

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.gui.{Launcher, Settings}
import lib.{FlockingLib, Movement2DSupport}

object BlobDroneSystemExploration extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.BlobDroneSystemExplorationDemo" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  //Settings.Sim_Topology = Topologies.Grid_LoVar
  Settings.Sim_NbrRadius = 0.05 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  //Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  Settings.Movement_Activator = (b: Any) => b.asInstanceOf[(Double, Double)]
  Settings.To_String = _ => ""
  Settings.Sim_DrawConnections = true
  Settings.Sim_realTimeMovementUpdate = false
  //Settings.Sim_Draw_Sensor_Radius = true
  launch()
}

/**
  * Scenario: the drones explore a region while keeping constant connection with the base.
  *  If the network gets disconnected, the drones are able to go back to the
  *  last known position of the base.
  *   - Sense1: drones
  *   - Sense2: obstacles
  *   - Sense3: base
  */
class BlobDroneSystemExplorationDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private val repulsionRange: Double = Settings.Sim_NbrRadius * 60.0 / 100
  private val obstacleForce: Double = 400.0

  private val separationThr = Settings.Sim_NbrRadius * 80.0
  private val neighboursThr = 4


  override def main(): (Double, Double) = rep(randomMovement(), (0.5,0.5))(behaviour)._1

  private def flockWithBase(myTuple: ((Double, Double),(Double,Double))): ((Double, Double),(Double,Double)) = {
    val myPosition = currentPosition()
    val gradient = distanceTo(sense3)
    val minGradHood = minHood(nbr(gradient))
    val nbrCount: Int = foldhoodPlus(0)(_ + _){1}
    val basePosition: (Double, Double) = broadcast(sense3, (myPosition.x, myPosition.y))
    mux(((gradient - minGradHood) > separationThr | gradient > 100) & nbrCount < neighboursThr) {
      val baseVector = goToPointWithSeparation(myTuple._2, repulsionRange)
      (baseVector, myTuple._2)
    } {
      val flockVector = flock(myTuple._1, Seq(sense1), Seq(sense2), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)

      mux(basePosition._1 > 1.0 | basePosition._2 > 1.0 | (basePosition._1 == 0.0 & basePosition._2 == 0.0)) {
        ((flockVector._1, flockVector._2), myTuple._2)
      } {
        ((flockVector._1, flockVector._2), basePosition)
      }
    }
  }

  private def behaviour(tuple:((Double, Double),(Double,Double))): ((Double, Double),(Double,Double)) = {
    val myPosition = currentPosition()
    mux(sense1){
      flockWithBase(tuple)
    }
    {
      mux(sense3){
        val bp: (Double, Double) = broadcast(sense3, (myPosition.x, myPosition.y))
        ((0.0,0.0), bp)
      }
      {
        val fv = flock(tuple._1, Seq(sense1), Seq(sense2), repulsionRange, 0.0, 0.0, repulsionForce, 0.0)
        (fv, (0.5,0.5))
      }
    }
  }

}