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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BuildingBlocks}
import it.unibo.scafi.simulation.gui.Settings
import sims.BasicDemo.launch

import scala.concurrent.duration._

object AgereDemoMain extends App {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.AgereDemo" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 50 // number of nodes
  launch()
}

class AgereDemo extends AggregateProgram with BuildingBlocks with SensorDefinitions {
  /* Parameters */
  val t_fail = (15 seconds)     // Time w/o failures
  val t_act = (5 seconds)     // Time for actuation
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
