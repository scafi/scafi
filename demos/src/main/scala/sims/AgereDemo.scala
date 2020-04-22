/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BuildingBlocks, TimeUtils}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

import scala.concurrent.duration._

object AgereDemoMain extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.AgereDemo" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 50 // number of nodes
  launch()
}

class AgereDemo extends AggregateProgram with BuildingBlocks with SensorDefinitions with TimeUtils {
  /* Parameters */
  val t_fail = (15.seconds)   // Time w/o failures
  val t_act = (5.seconds)     // Time for actuation
  val D_alert = 10.0          // People density threshold
  val radius = 20             // Radius of monitoring areas
  val meanDist = radius*2     // Mean distance between area leaders

  /* Program result type */
  type Result = (DeviceStatus, IsActing)

  trait DeviceStatus
  case object Ok extends DeviceStatus
  case object Alert extends DeviceStatus
  case object FailedRecently extends DeviceStatus

  trait IsActing
  case object Acts extends IsActing
  case object Idle extends IsActing

  /* Core logic */
  def main = rep[Result]((Ok,Idle)){ case (lastStatus, wasActing) =>
    var isActing = if(recentlyTrue(t_act, lastStatus==Alert)) {
      act()
      Acts
    } else{
      Idle
    }

    (branch(withoutFailuresSince(t_fail)) {
      val areaLeader = S(grain = meanDist, metric = nbrRange)
      val D_mean = average(areaLeader, senseLocalDensity())

      branch[DeviceStatus](D_mean > D_alert){
        Alert
      }{ // Branch of working devices sensing low density
        Ok
      }
    }{ // Branch of devices that have failed recently
      FailedRecently
    }, isActing)
  }

  /* Functions */
  def senseLocalDensity() = foldhood(0)(_+_){ nbr(1) }
  def withoutFailuresSince(d: Duration): Boolean = !recentlyTrue(d, sense2)
  def act[T](): Unit = { }

  /* Utility functions */
  def now = System.nanoTime()
  def never = Long.MaxValue
}
