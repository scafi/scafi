/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, ScafiFCLanguage}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object FCRun extends Launcher {
  Settings.Sim_ProgramClass = "sims.FCGradient"
  Settings.ShowConfigPanel = false
  Settings.Sim_NbrRadius = 0.15
  Settings.Sim_NumNodes = 100
  launch()
}

class FCGradient extends AggregateProgram with ScafiFCLanguage with SensorDefinitions {
  override def main() = gradient(sense1)

  def gradient(src: Boolean) = rep(Double.PositiveInfinity)(d => {
    mux(src){ 0.0 }{
      (nbrField(d) + nbrRange).withoutSelf.minHood
    }
  })
}
