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

package frontend.sims.standard

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockT}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

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
class SimpleTimer extends AggregateProgram with SensorDefinitions with BlockT {
  override def main() = branch(sense1){timer(10000)}{0}
}
