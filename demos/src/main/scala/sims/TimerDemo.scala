/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

import scala.concurrent.duration.DurationInt

object TimerDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.RecentEvent" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

class SimpleTimer extends AggregateProgram with SensorDefinitions with BlockT {
  override def main(): Int = branch(sense1){timer(100)}{0}
}

class RecentEvent extends AggregateProgram with SensorDefinitions with TimeUtils with FieldUtils {
  override def main(): Boolean = recentlyTrue(10.seconds, includingSelf.anyHood(nbr{ sense1 }))
}
