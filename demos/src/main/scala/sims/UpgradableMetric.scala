/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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
      branch(rep(0)(_ + 1) < 100){
        Fun[this.type,Double](1, (p) => p.nbrRange())
      }{
        Fun[this.type,Double](1, (p) => p.nbrRange() + 5)
      }
    }
    val metric = up[this.type,Double](injecter)
    distanceTo(sense1, ()=>metric.fun(this))
  }
}