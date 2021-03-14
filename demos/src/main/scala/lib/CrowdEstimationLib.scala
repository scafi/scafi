/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package lib

import java.util.concurrent.TimeUnit

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.space.{Point2D, Point3D}
import sims.SensorDefinitions

import scala.concurrent.duration.FiniteDuration

/**
  * See papers:
  * - Building blocks for aggregate programming of self-organising applications (Beal, Viroli, 2014)
  * - Aggregate Programming for the Internet of Things (Beal et al., IEEE Computer, 2015)
  */
trait CrowdEstimationLib extends BuildingBlocks { self: AggregateProgram with SensorDefinitions =>
  val (high,low,none) = (2,1,0) // crowd level
  def managementRegions(grain: Double, metric: Metric): Boolean =
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
    branch(state) {
      true
    } {
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
    mux(crowdRgn) { dangerousDensity(p, r) } { none }
  }

  /**
    * Density is estimated as ρ = |nbrs|/pπr2w, where |nbrs| counts neighbors
    * within range r, p estimates the proportion of people with a device running
    * the app (about 0.5 percent of marathon attendees), and w estimates the
    * fraction of walkable space in the local urban environment.
    */
  def countNearby(range: Double): Double =  {
    // val human = rep(h <- env.get("role")==0) { h };
    excludingSelf.sumHood(mux(/*human &&*/ nbrRange() < range) { 1 } { 0 })
  }

  def densityEstimation(p: Double, range: Double, w: Double): Double = {
    countNearby(range) / (p * Math.PI * Math.pow(range, 2) * w)
  }

  def isRecentEvent (event: Boolean, timeout: Double): Boolean = {
    branch ( event ) {
      true
    } { timerLocalTime( FiniteDuration(timeout.toLong, TimeUnit.SECONDS) ) > 0}
  }

  /**
    * def dangerousDensity(p, range, dangerousDensity, groupSize, w) {
    *   let partition = S(range, nbrRange);
    *   let localDensity = densityEstimation(p, range, w);
    *   let avg = summarize(partition, sum, localDensity, 0) / summarize(partition, sum, 1, 0);
    *   let count = summarize(partition, sum, 1 / p, 0);
    *   avg > dangerousDensity && count > groupSize
    * }
    */
  def dangerousDensityFull(p: Double, range: Double, dangerousDensity: Double, groupSize: Double, w: Double): Boolean = {
    val partition = S(range, nbrRange)
    val localDensity = densityEstimation(p, range, w)
    val avg = summarize(partition, _+_, localDensity, 0.0) / summarize(partition, _+_, 1.0, 0.0)
    val count = summarize(partition, _+_, 1.0 / p, 0.0)
    broadcast(partition, avg > dangerousDensity && count > groupSize)
  }

  /**
    * def crowdTracking(p, range, w, crowdedDensity, dangerousThreshold, groupSize, timeFrame) {
    *   let densityEst = densityEstimation(p, range, w)
    *   env.put("densityEst", densityEst)
    *   if (isRecentEvent(densityEst > crowdedDensity, timeFrame)) {
    *     if (dangerousDensity(p, range, dangerousThreshold, groupSize, w)) { overcrowded() } else { atRisk() }
    *   } else { none() }
    * }
    * @param p
    * @param range
    * @param w
    * @param crowdedDensity
    * @param dangerousThreshold
    * @param groupSize
    * @param timeFrame
    */
  def crowdTrackingFull(p: Double,
                        range: Double,
                        w: Double,
                        crowdedDensity: Double,
                        dangerousThreshold: Double,
                        groupSize: Double,
                        timeFrame: Double): Crowding = {
    val densityEst = densityEstimation(p, range, w)
    mux(isRecentEvent(densityEst > crowdedDensity, timeFrame)){
      if(dangerousDensityFull(p, range, dangerousThreshold, groupSize, w)){ Overcrowded } else AtRisk
    }{ Fine }
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

  sealed trait Crowding
  case object Overcrowded extends Crowding
  case object AtRisk extends Crowding
  case object Fine extends Crowding

  val noAdvice = Point2D(Double.NaN, Double.NaN)

  /**
    * def direction(radius, crowding) {
    *   mux (distanceTo(crowding == atRisk()) < radius) {
    *   vectorFrom(crowding == overcrowded())
    *   } else { noAdvice() }
    * }
    */
  def direction(radius: Double, crowding: Crowding): Point3D =
    mux(distanceTo(crowding == AtRisk) < radius){ vectorFrom(crowding == Overcrowded) }{ currentPosition() }

  /**
    * def vectorFrom(target) {
    *   let selfCoords = self.getCoordinates();
    *   let targetCoords = broadcast(target, self.getCoordinates());
    *   let xy = 2 * selfCoords - targetCoords;
    *   let k = 2.5;
    *   let lat = (xy.get(1) + selfCoords.get(1)*(k-1))/k;
    *   let long = (xy.get(0) + selfCoords.get(0)*(k-1))/k;
    *   env.put("self + target coords + xy + lat + long", selfCoords + " " + targetCoords + " " + xy + " " + lat + " " + long);
    *   if (isFinite(lat) && isFinite(long)) { [lat, long] } else { noAdvice() }
    * }
    */
  def vectorFrom(target: Boolean): Point3D = {
    val Point3D(x,y,_) = currentPosition()
    val Point3D(xt,yt,_) = broadcast[Point3D](target, currentPosition())
    Point3D((x + xt)/2, (y + yt)/2, 0)
  }
}
