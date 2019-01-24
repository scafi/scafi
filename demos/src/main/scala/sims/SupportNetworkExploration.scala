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
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}
import lib.{FlockingLib, Movement2DSupport}

object SupportNetworkExploration extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.SupportNetworkExplorationDemo" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  //Settings.Sim_Topology = Topologies.Grid_LoVar
  Settings.Sim_NbrRadius = 0.05 // neighbourhood radius
  Settings.Sim_NumNodes = 200 // number of nodes
  //Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  Settings.Movement_Activator = (b: Any) => b.asInstanceOf[(Double, Double)]
  Settings.To_String = _ => ""
  Settings.Sim_DrawConnections = true
  Settings.Sim_realTimeMovementUpdate = false
  //Settings.Sim_Draw_Sensor_Radius = true
  launch()
}

/**
  * Scenario: exploration from a fixed or mobile base.
  *  The nodes should explore the space without losing connections with the base;
  *  when this happens, they should return to the base.
  *  Other nodes build flocks with loose rules, sporadically interacting with the main network.
  *   - sense1: nodes of the main network;
  *   - sense2: obstacles.
  *   - sense3: base of operations;
  *   - sense4: explorer nodes, connected to the network.
  */
class SupportNetworkExplorationDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private val repulsionRange: Double = Settings.Sim_NbrRadius * 60.0 / 100
  private val obstacleForce: Double = 100.0
  private val separationThr: Double = Settings.Sim_NbrRadius * 80.0
  private val neighboursThr: Int = 7
  private val SPACE_DIMENSION: Int = 100

  override def main():(Double, Double) = rep(randomMovement(), (0.5, 0.5))(round)._1

  private def behaviourSense1(tuple: ((Double, Double), (Double, Double))) = {
    var gradient = Double.MaxValue
    var minGradHood = 0.0
    branch(sense4)() {
      gradient = distanceTo(sense3)
      minGradHood = minHood(nbr(gradient))
    }
    mux((gradient - minGradHood) > separationThr | gradient > SPACE_DIMENSION) ((goToPointWithSeparation(tuple._2,repulsionRange), tuple._2)) {
      var temp = flock(tuple._1, Seq(sense1), Seq(sense2), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      temp = ((temp._1 + .001)/2, (temp._2 + .001)/2)
      val basePosition: (Double, Double) = branch(sense4){(0.0,0.0)}{broadcast(sense3, (currentPosition().x, currentPosition().y))}
      mux(basePosition._1 > 1.0 | basePosition._2 > 1.0 | (basePosition._1 == 0.0 & basePosition._2 == 0.0))((temp, tuple._2))((temp, basePosition))
    }
  }

  private def behaviourSense4(tuple: ((Double, Double), (Double, Double))) = {
    val gradient = distanceTo(sense1)
    val minGradHood = minHood(nbr(gradient))
    val nbrCount: Int = foldhoodPlus(0)(_ + _){1}
    mux(((gradient - minGradHood) > separationThr * 2 | gradient > SPACE_DIMENSION) & nbrCount < neighboursThr) {
      val s = goToPointWithSeparation(tuple._2, repulsionRange)
      (s, tuple._2)
    } {
      val temp = flock(tuple._1, Seq(sense4), Seq(sense1), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      val p: (Double, Double) = broadcast(sense1, (currentPosition().x, currentPosition().y))
      mux(p._1 > 1.0 | p._2 > 1.0 | (p._1 == 0.0 & p._2 == 0.0)) {
        ((temp._1, temp._2), tuple._2)
      } {
        ((temp._1, temp._2), p)
      }
    }
  }

  private def round(tuple: ((Double, Double), (Double, Double))): ((Double, Double), (Double, Double)) =
    mux(sense1) {
      behaviourSense1(tuple)
    } (
      mux(sense4){
        behaviourSense4(tuple)
      } {
        mux(sense3){
          ((.0,.0),(.0,.0))
        } {
          (flock(tuple._1, Seq(sense1), Seq(sense2), 0.04, 0.0, 0.0, 10.0, 0.0), tuple._2)
        }
      }
    )

}