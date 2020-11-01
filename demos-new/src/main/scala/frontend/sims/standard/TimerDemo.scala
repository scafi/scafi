/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.standard

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, BlockT}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

object TimerDemo extends App {
  ScafiProgramBuilder (
    Random(50,500,500),
    SimulationInfo(program = classOf[SimpleTimer]),
    RadiusSimulation(radius = 140),
    neighbourRender = true
  ).launch()
}
/**
  * with this simulation you can see how you can interact with simulation cycle.
  * select a set of node an mark it as sensor1. the number in output of each node
  * start to the value 10000 and decrease until 0. if you click 5 the simulation stop
  * and the number remain in the same value. if click you 6 the simulation continue in
  * the same state. if you click 7 the simulation restart (each node show 10000).
  */
class SimpleTimer extends ScafiStandardAggregateProgram with SensorDefinitions with BlockT {
  override def main() = branch(sense1){timer(10000)}{0}
}
