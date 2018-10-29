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


object BasicMovementDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.BasicMovement" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  //Settings.Sim_Topology = Topologies.Grid_LoVar
  Settings.Sim_NbrRadius = 0.03 // neighbourhood radius
  Settings.Sim_NumNodes = 1000 // number of nodes
  //Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  Settings.Movement_Activator = (b: Any) => b.asInstanceOf[(Double, Double)]
  Settings.To_String = _ => ""
  Settings.Sim_DrawConnections = true
  Settings.Sim_realTimeMovementUpdate = false
  //Settings.Sim_Draw_Sensor_Radius = true
  launch()
}

class BasicMovement extends AggregateProgram with SensorDefinitions with FlockingLib with BlockG with Movement2DSupport {
  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private val repulsionRange: Double = Settings.Sim_NbrRadius * 60.0 / 200
  private val obstacleForce: Double = 400.0

  override def main:(Double, Double) = rep(randomMovement())(behaviour2)

  private def behaviour1(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      flock(tuple, Seq(sense1), Seq(sense3), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
    } {
      tuple
    }

  private def behaviour2(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      clockwiseRotation(.5, .5)
    } {
      (.0, .0)
    }

  private def behaviour3(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      val m = clockwiseRotation(.5, .5)
      val f = flock(tuple, Seq(sense1), Seq(sense3), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      normalizeToScale(m._1 + f._1, m._2 + f._2)
    } {
      (.0, .0)
    }
}