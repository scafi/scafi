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

  trait StandardPlatformSensorNames {
    val LSNS_RANDOM: CNAME = CNAMEfromString("LSNS_RANDOM")
  }
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

  trait StandardTemporalSensorNames {
    val LSNS_TIME: CNAME = CNAMEfromString("LSNS_TIME")
    val LSNS_TIMESTAMP: CNAME = CNAMEfromString("LSNS_TIMESTAMP")
    val LSNS_DELTA_TIME: CNAME = CNAMEfromString("LSNS_DELTA_TIME")
    val NBR_LAG: CNAME = CNAMEfromString("NBR_LAG")
    val NBR_DELAY: CNAME = CNAMEfromString("NBR_DELAY")
  }
}

trait SpaceAwarePlatform extends Platform {
  self: Platform.PlatformDependency with MetricSpatialAbstraction =>

  trait SpaceAwareDevice {
    def currentPosition(): P

    def nbrRange(): D
    def nbrVector(): P
  }

  trait StandardSpatialSensorNames {
    val LSNS_POSITION: CNAME = CNAMEfromString("LSNS_POSITION")
    val NBR_VECTOR: CNAME = CNAMEfromString("NBR_VECTOR")
    val NBR_RANGE: CNAME = CNAMEfromString("NBR_RANGE")
  }
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
    def localSensor[A](name: CNAME)(id: ID): A
    def nbrSensor[A](name: CNAME)(id: ID)(idn: ID): A
    def export(id: ID): Option[EXPORT]
    def exports(): Map[ID, Option[EXPORT]]
    def sensorState(filter: (CNAME,ID) => Boolean = (s,n) => true): collection.Map[CNAME, collection.Map[ID,Any]]
    def neighbouringSensorState(filter: (CNAME,ID,ID) => Boolean = (s,n,nbr) => true): collection.Map[CNAME, collection.Map[ID, collection.Map[ID, Any]]]
  }
}

object SimulationPlatform {
  type PlatformDependency = SpaceTimeAwarePlatform.PlatformDependency
}
