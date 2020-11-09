/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, ScafiStandardLibraries}
import ScafiStandardLibraries.BlockG
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}
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

class BasicMovement extends ScafiStandardAggregateProgram with SensorDefinitions with FlockingLib with BlockG with Movement2DSupport {
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