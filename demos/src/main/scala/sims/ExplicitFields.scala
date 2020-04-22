/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, ExplicitFields}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object ExplicitFieldsRun extends Launcher {
  Settings.Sim_ProgramClass = "sims.GradientWithExplicitFields"
  Settings.ShowConfigPanel = false
  Settings.Sim_NbrRadius = 0.15
  Settings.Sim_NumNodes = 100
  launch()
}

class GradientWithExplicitFields extends AggregateProgram with SensorDefinitions with ExplicitFields {
  override def main() = gradient(sense1)

  def gradient(src: Boolean) = rep(Double.PositiveInfinity)(d => {
    mux(src){ 0.0 }{
      (fnbr(d) + fsns(nbrRange)).minHoodPlus
    }
  })
}
