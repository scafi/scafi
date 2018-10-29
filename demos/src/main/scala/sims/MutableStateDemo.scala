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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, ID}
import it.unibo.scafi.simulation.gui.{Launcher, Settings}

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
class MutableStateProgram extends AggregateProgram {
  var m1: Map[ID,Int] = Map()

  override def main() = {
    m1 += mid -> 2
    val m2 = rep(MMap[ID,Int]()) { x => x }
    m2.put(mid, 9)
    s"$m1 $m2"
  }
}
