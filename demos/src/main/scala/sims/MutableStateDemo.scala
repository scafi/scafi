/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, ID}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

import scala.collection.mutable.{Map => MMap}

object MutableStateDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.MutableStateProgram"
  Settings.ShowConfigPanel = false
  Settings.Sim_NbrRadius = 0.45
  Settings.Sim_NumNodes = 3
  launch()
}

/**
  * Shows:
  *  - the impact of using mutable fields within AggregatePrograms.
  *  - the use of REP to correctly keep track of state.
  */
class MutableStateProgram extends ScafiStandardAggregateProgram {
  var m1: Map[ID,Int] = Map()

  override def main() = {
    m1 += mid -> 2
    val m2 = rep(MMap[ID,Int]()) { x => x }
    m2.put(mid, 9)
    s"$m1 $m2"
  }
}
