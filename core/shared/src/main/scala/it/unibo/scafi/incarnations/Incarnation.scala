/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.incarnations

import it.unibo.scafi.core.{Core, Engine, RichLanguage}
import it.unibo.scafi.platform.SpaceTimeAwarePlatform
import it.unibo.scafi.space.BasicSpatialAbstraction
import it.unibo.scafi.time.TimeAbstraction

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

trait Incarnation extends Core
  with Engine
  with RichLanguage
  with SpaceTimeAwarePlatform
  with BasicSpatialAbstraction
  with TimeAbstraction {

  trait FieldCalculusSyntax extends Constructs with Builtins

  trait AggregateComputation[T] extends ExecutionTemplate with FieldCalculusSyntax with Serializable {
    type MainResult = T
  }

  trait AggregateInterpreter extends ExecutionTemplate with FieldCalculusSyntax with Serializable {
    type MainResult = Any
  }

  trait AggregateProgram extends AggregateInterpreter

  class BasicAggregateInterpreter extends AggregateInterpreter {
    override def main(): MainResult = ???
  }

  trait StandardSensorNames extends StandardPlatformSensorNames
    with StandardTemporalSensorNames
    with StandardSpatialSensorNames

  trait StandardSensors extends StandardSensorNames {
    self: Constructs =>

    /**
      * Time forward view: expected time from the device computation to neighbor's next computation
      * incorporating that information.
      * For device's neighbors, it is the best estimate that the underlying system can provide.
      * For the current device, it is like deltaTime().
      *
      * This is the idea, pictorially:
      *
      *                         PAST                    PRESENT   FUTURE
      * TIME ----------------------------------------------|-------------->
      *              X           N                         X
      *                          ------------lag------------
      *              ================deltatime==============
      *                          ================deltatime==============
      *              $$$$delay$$$$                          $$$delay$$$$
      */
    def nbrDelay(): FiniteDuration = nbrvar[FiniteDuration](NBR_DELAY)

    /**
      * Time backward view: how long ago information from neighbors was received.
      * For device's neighbors, it is the time of the computation minus the timestamp on the packet.
      * Dropped packets temporarily increase nbrLag.
      * For the current device, it is like deltaTime().
      */
    def nbrLag(): FiniteDuration = nbrvar[FiniteDuration](NBR_LAG)

    /**
      * Get the distance between the current device and its neighbors.
      */
    def nbrRange(): D = nbrvar[D](NBR_RANGE)

    /**
      * Get the direction vectors towards neighbours.
      * @return a point of type P, assuming the currently executing device is the origin
      */
    def nbrVector(): P = nbrvar[P](NBR_VECTOR)

    /**
      * @return the current position in space
      */
    def currentPosition(): P = sense[P](LSNS_POSITION)

    /**
      * @return the current local time
      */
    def currentTime(): Time = sense[Time](LSNS_TIME)

    /**
      * @return the current time in milliseconds since epoch
      */
    def timestamp(): Long = sense[Long](LSNS_TIMESTAMP)

    /**
      * @return the duration since the last round of execution
      */
    def deltaTime(): FiniteDuration = sense[FiniteDuration](LSNS_DELTA_TIME)

    /**
      * @return a random double from 0 to 1
      */
    def randomGenerator(): Random = sense[Random](LSNS_RANDOM)

    /**
      * @return a random double from 0 to 1
      */
    def nextRandom(): Double = randomGenerator().nextDouble()
  }
}
