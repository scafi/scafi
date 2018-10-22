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

package old.sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, ExplicitFields}
import it.unibo.scafi.simulation.old.gui.{Launcher, Settings}

object ExplicitFieldsRun extends Launcher {
  Settings.Sim_ProgramClass = "old.sims.GradientWithExplicitFields"
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
