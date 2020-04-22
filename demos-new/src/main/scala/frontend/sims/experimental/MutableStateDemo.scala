/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.experimental

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, ID}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

import scala.collection.mutable.{Map => MMap}

object MutableStateDemo extends App {
  ScafiProgramBuilder (
    Random(10,500,500),
    SimulationInfo(program = classOf[MutableStateProgram]),
    RadiusSimulation(radius = 200),
    neighbourRender = true
  ).launch()
}

/**
  * Shows:
  *  - the impact of using mutable fields within AggregatePrograms.
  *  - the use of REP to correctly keep track of state.
  */
@Demo
class MutableStateProgram extends AggregateProgram {
  var m1: Map[ID,Int] = Map()

  override def main() = {
    m1 += mid -> 2
    val m2 = rep(MMap[ID,Int]()) { x => x }
    m2.put(mid, 9)
    s"$m1 $m2"
  }
}
