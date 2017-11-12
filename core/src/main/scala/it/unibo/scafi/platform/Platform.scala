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
  val NBR_RANGE_NAME: NSNS
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

  val LSNS_RANDOM: LSNS

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
