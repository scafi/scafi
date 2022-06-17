/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.experimental

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BuildingBlocks, TimeUtils}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

import scala.concurrent.duration._

object AgereDemoMain extends App {
  ScafiProgramBuilder (
    Random(50,500,500),
    SimulationInfo(program = classOf[AgereDemo]),
    RadiusSimulation(radius = 140),
    neighbourRender = true
  ).launch()
}
@Demo
class AgereDemo extends AggregateProgram with BuildingBlocks with SensorDefinitions with TimeUtils {
  /* Parameters */
  val t_fail: FiniteDuration = (15.seconds)     // Time w/o failures
  val t_act: FiniteDuration = (5.seconds)     // Time for actuation
  val D_alert = 10.0          // People density threshold
  val radius = 20             // Radius of monitoring areas
  val meanDist: Int = radius*2     // Mean distance between area leaders

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
  def main(): Result = rep[Result]((Ok,Idle)){ case (lastStatus, wasActing) =>
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
  def senseLocalDensity(): Int = foldhood(0)(_+_){ nbr(1) }
  def withoutFailuresSince(d: Duration): Boolean = !recentlyTrue(d, sense2)
  def act[T](): Unit = { }

  /* Utility functions */
  def now: Long = System.nanoTime()
  def never = Long.MaxValue
}
