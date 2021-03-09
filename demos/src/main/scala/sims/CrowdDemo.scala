/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, Builtins}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}
import lib.CrowdEstimationLib

object CrowdDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.Crowd" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.1 // neighbourhood radius: 10 m
  Settings.Sim_NumNodes = 200 // number of nodes
  launch()
}

/**
  * See papers:
  * - Building blocks for aggregate programming of self-organising applications (Beal, Viroli, 2014)
  * - Aggregate Programming for the Internet of Things (Beal et al., IEEE Computer, 2015)
  */
class Crowd extends AggregateProgram  with SensorDefinitions with BlockG with CrowdEstimationLib {
  override def main(): Any = {
    /* CROWD ESTIMATION
    *  * In FOCAS:
    * * p = 0.1; range = 15 // 30; wRange = 30 // 100; commRange = n.a.; avgThreshold = 2.17 people / m²;
    * sumThreshold = 300 people; maxDensity = 1.08 people / m²; timeFrame = 60; w = 0.25 (fraction of walkable space in the local urban environment)
    * */
    val distToRiskZone = 30.0;
    val p = 0.005
    val crowdRange = 30
    val w = 0.25
    val crowdedDensity = 1.08
    val dangerousThreshold = 2.17
    val groupSize = 10 // 300
    val timeFrame = 60
    val densityEst = densityEstimation(p, crowdRange, w)
    val danger = dangerousDensityFull(p, crowdRange, dangerousThreshold, groupSize, w)
    val crowding = crowdTrackingFull(p, crowdRange, w, crowdedDensity, dangerousThreshold, groupSize, timeFrame) // overcrowded(), atRisk(), or none()
    val goto = direction(distToRiskZone, crowding)
    val partition = S(crowdRange, nbrRange)  // ISSUE: THIS DOES NOT STABILISE
    partition
  }
}
