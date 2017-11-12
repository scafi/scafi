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

package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import sims.SensorDefinitions

trait CrowdEstimationLib extends BuildingBlocks { self: AggregateProgram with SensorDefinitions =>
  /***********************************/
  /* IEEE Computer: Crowd estimation */
  /***********************************/

  val (high,low,none) = (2,1,0) // crowd level
  def managementRegions(grain: Double, metric: => Double): Boolean =
    S(grain, metric) /*{
    breakUsingUids(randomUid, grain, metric)
  }*/

  def unionHoodPlus[A](expr: => A): List[A] =
    foldhoodPlus(List[A]())(_++_){ List[A](expr) }

  def densityEst(p: Double, range: Double): Double = {
    val nearby = unionHoodPlus(
      mux (nbrRange < range) { nbr(List(mid())) } { List() }
    )

    val footprint = 1 /*if(self.hasEnvironmentVariable("footprint")) {
      self.getEnvironmentVariable("footprint") } else { 1 }*/
    nearby.size / p / (Math.PI * Math.pow(range,2) * footprint)
  }

  def rtSub(started: Boolean, state: Boolean, memoryTime: Double): Boolean = {
    if(state) {
      true
    } else {
      limitedMemory[Boolean,Double](started, false, memoryTime)._1
    }
  }

  def recentTrue(state: Boolean, memoryTime: Double): Boolean = {
    rtSub(timer(10) == 0, state, memoryTime)
  }

  def dangerousDensity(p: Double, r: Double) = {
    val mr = managementRegions(r*2, nbrRange)
    val danger = average(mr, densityEst(p, r)) > 2.17 &&
      summarize(mr, (_:Double)+(_:Double), 1 / p, 0) > 300
    if(danger) { high } else { low }
  }

  def crowdTracking(p: Double, r: Double, t: Double) = {
    val crowdRgn = recentTrue(densityEst(p, r)>1.08, t)
    if(crowdRgn) { dangerousDensity(p, r) } else { none }
  }

  /**
    *
    * @param p estimates the proportion of people with a device running the app
    * @param r is the range in which the neighbours are counted
    * @param warn estimates fraction of walkable space in the local urban env
    * @param t is the memory time
    * @return a boolean indicating whether there is warning or not.
    */
  def crowdWarning(p: Double, r: Double, warn: Double, t: Double): Boolean = {
    distanceTo(crowdTracking(p,r,t) == high) < warn
  }
}
