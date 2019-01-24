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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.Settings
import sims.BasicDemo.launch

object UpgradableMetricDemo extends App {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.UpgradableMetricProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

class UpgradableMetricProgram extends AggregateProgram with BuildingBlocks with SensorDefinitions with DynamicCode
  with FieldUtils with BlockG {
  override def main(): Any = {
    val injecter: Injecter[this.type,Double] = () => {
      branch(rep(0)(_+1)<100){
        Fun[this.type,Double](1, (p) => p.nbrRange())
      }{
        Fun[this.type,Double](1, (p) => p.nbrRange()+5)
      }
    }
    val metric = up[this.type,Double](injecter)
    distanceTo(sense1, ()=>metric.fun(this))
  }
}