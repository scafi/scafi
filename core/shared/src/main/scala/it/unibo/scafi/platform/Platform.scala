/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.platform

import it.unibo.scafi.core.{Core, Engine}
import it.unibo.scafi.space.MetricSpatialAbstraction
import it.unibo.scafi.time.TimeAbstraction

import scala.concurrent.duration.FiniteDuration

/**
 * This trait defines a component that requires to be "attached" to Core
 * It defines a whole platform view, with a NETWORK type modelling the whole computational system
 */
trait Platform {
  self: Platform.PlatformDependency =>

  val LSNS_RANDOM: LSNS
}

object Platform {
  type PlatformDependency = Core with Engine
}

trait TimeAwarePlatform extends Platform {
  self: Platform.PlatformDependency with TimeAbstraction =>

  trait TimeAwareDevice {
    def currentTime(): Time
    def timestamp(): Long
    //def lastExecutionTime(): Time
    def deltaTime(): FiniteDuration

    //def nbrLastExecutionTime(): Time
    def nbrDelay(): FiniteDuration
    def nbrLag(): FiniteDuration
  }

  val LSNS_TIME: LSNS
  val LSNS_TIMESTAMP: LSNS
  val LSNS_DELTA_TIME: LSNS
  val NBR_LAG: NSNS
  val NBR_DELAY: NSNS
}

trait SpaceAwarePlatform extends Platform {
  self: Platform.PlatformDependency with MetricSpatialAbstraction =>

  trait SpaceAwareDevice {
    def currentPosition(): P

    def nbrRange(): D
    def nbrVector(): P
  }

  val LSNS_POSITION: LSNS
  val NBR_VECTOR: NSNS
  val NBR_RANGE: NSNS
}

trait SpaceTimeAwarePlatform extends SpaceAwarePlatform with TimeAwarePlatform {
  self: SpaceTimeAwarePlatform.PlatformDependency =>

  trait SpaceTimeAwareDevice
    extends SpaceAwareDevice
      with TimeAwareDevice
}

object SpaceTimeAwarePlatform {
  type PlatformDependency = Platform.PlatformDependency with MetricSpatialAbstraction with TimeAbstraction
}

trait SimulationPlatform extends SpaceTimeAwarePlatform {
  self: SpaceTimeAwarePlatform.PlatformDependency =>

  type NETWORK <: Network

  trait Network {
    def ids: Set[ID]
    def neighbourhood(id: ID): Set[ID]
    def localSensor[A](name: LSNS)(id: ID): A
    def nbrSensor[A](name: NSNS)(id: ID)(idn: ID): A
    def export(id: ID): Option[EXPORT]
    def exports(): Map[ID, Option[EXPORT]]
  }
}

object SimulationPlatform {
  type PlatformDependency = SpaceTimeAwarePlatform.PlatformDependency
}
