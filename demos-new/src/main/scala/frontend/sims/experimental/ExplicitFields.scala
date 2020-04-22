/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.experimental

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, ExplicitFields}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

object ExplicitFieldsRun extends App {
  ScafiProgramBuilder (
    Random(50,500,500),
    SimulationInfo(program = classOf[GradientWithExplicitFields]),
    RadiusSimulation(radius = 140),
    neighbourRender = true
  ).launch()
}

class GradientWithExplicitFields extends AggregateProgram with SensorDefinitions with ExplicitFields {
  override def main() = gradient(sense1)

  def gradient(src: Boolean) = rep(Double.PositiveInfinity)(d => {
    mux(src){ 0.0 }{
      (fnbr(d) + fsns(nbrRange)).minHoodPlus
    }
  })
}
