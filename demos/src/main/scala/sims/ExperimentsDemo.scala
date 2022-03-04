/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}
import java.time.{LocalDateTime, ZoneOffset}
import java.time.temporal.ChronoUnit

import it.unibo.scafi.space.Point3D
import sims.DoubleUtils.Precision

import scala.concurrent.duration.FiniteDuration


object ExperimentsDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.ExperimentsProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  Settings.ConfigurationSeed = 0
  launch()
}

class ExperimentsProgram extends AggregateProgram with SensorDefinitions with FieldUtils {
  def main: (ID, Int, Option[ID]) = (mid,
    {
      val numSrcNbrs = foldhood(0)(_ + _)(if(nbr{sense1}) 1 else 0)
      includingSelf.minHoodSelector(nbr{-numSrcNbrs}){ nbr(mid) }
    },
    {
      val numSrcNbrs = foldhood(0)(_ + _)(if(nbr{sense1}) 1 else 0)
      excludingSelf.minHoodSelector(nbr{numSrcNbrs}){ nbr(mid) }
    }
  )
}
