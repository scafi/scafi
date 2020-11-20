/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.experimental

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import ScafiStandardLibraries._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

object CollectionDemo extends App {
  ScafiProgramBuilder (
    Random(50,500,500),
    SimulationInfo(program = classOf[Collection]),
    RadiusSimulation(radius = 140),
    neighbourRender = true
  ).launch()
}

@Demo
class Collection extends ScafiStandardAggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  override def main() = summarize(sense1, _ + _, if (sense2) 1.0 else 0.0, 0.0)
}


