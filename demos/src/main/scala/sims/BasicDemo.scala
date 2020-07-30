/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings, SettingsSpace}

import scala.concurrent.duration._

object BasicDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.BasicProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 1 // neighbourhood radius
  Settings.Sim_NumNodes = 10 // number of nodes
  Settings.Sim_Topology = SettingsSpace.Topologies.Grid
  launch()
}

class BasicProgram extends AggregateProgram with Gradients with SensorDefinitions with StandardSensors with GenericUtils {

  def fun: Int => Int = _ - 1
  def fun2: Int =>Int = _ / 2
  //expectedResult: 1
  override def main() = BISGradient(sense1)
  //override  def main() = cyclicFunctionWithDecay(10, 1,() => true, cycle)
}
