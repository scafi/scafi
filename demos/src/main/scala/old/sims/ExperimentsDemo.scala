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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.old.gui.{Launcher, Settings}

object ExperimentsDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "old.sims.ExperimentsProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  Settings.ConfigurationSeed = 0
  launch()
}

class ExperimentsProgram extends AggregateProgram with SensorDefinitions with FieldUtils {
  def main = (mid,
    {
      val numSrcNbrs = foldhood(0)(_+_)(if(nbr{sense1}) 1 else 0)
      includingSelf.minHoodSelector(nbr{-numSrcNbrs}){ nbr(mid) }
    },
    {
      val numSrcNbrs = foldhood(0)(_+_)(if(nbr{sense1}) 1 else 0)
      excludingSelf.minHoodSelector(nbr{numSrcNbrs}){ nbr(mid) }
    }
  )
}
