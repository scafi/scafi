/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.incarnations

import it.unibo.scafi.core.{Core, Engine}
import it.unibo.scafi.languages.FieldCalculusLanguages
import it.unibo.scafi.languages.scafistandard.RichLanguage
import it.unibo.scafi.platform.SpaceTimeAwarePlatform
import it.unibo.scafi.space.BasicSpatialAbstraction
import it.unibo.scafi.time.TimeAbstraction

import scala.concurrent.duration.FiniteDuration
import scala.language.higherKinds
import scala.util.Random

trait Incarnation extends Core
  with Engine
  with RichLanguage
  with SpaceTimeAwarePlatform
  with BasicSpatialAbstraction
  with TimeAbstraction
  with FieldCalculusLanguages {

  trait AggregateComputation[T] extends ExecutionTemplate with Serializable {
    self: LanguageSemantics =>
    type MainResult = T
  }

  trait ScafiStandardAggregateComputation[T] extends AggregateComputation[T] with ScafiStandardLanguage

  trait AggregateInterpreter extends ExecutionTemplate with Serializable {
    self: LanguageSemantics =>
    type MainResult = Any
  }

  trait ScafiStandardAggregateInterpreter extends AggregateInterpreter with ScafiStandardLanguage

  trait AggregateProgram extends AggregateInterpreter {
    self: LanguageSemantics =>
  }

  trait ScafiStandardAggregateProgram extends ScafiStandardAggregateInterpreter with AggregateProgram

  class BasicAggregateInterpreter extends ScafiStandardAggregateInterpreter {
    override def main(): MainResult = ???
  }

  trait StandardSensorNames extends StandardPlatformSensorNames
    with StandardTemporalSensorNames
    with StandardSpatialSensorNames

  trait StandardSensors extends StandardLocalSensors with StandardNeighbourhoodSensors {
    self: LocalSensorReader with NeighbourhoodSensorReader =>
  }

  trait StandardLocalSensors extends StandardSensorNames {
    self: LocalSensorReader =>

    /**
     * @return the current position in space
     */
    def currentPosition(): P = readLocalSensor[P](LSNS_POSITION)

    /**
     * @return the current local time
     */
    def currentTime(): Time = readLocalSensor[Time](LSNS_TIME)

    /**
     * @return the current time in milliseconds since epoch
     */
    def timestamp(): Long = readLocalSensor[Long](LSNS_TIMESTAMP)

    /**
     * @return the duration since the last round of execution
     */
    def deltaTime(): FiniteDuration]= readLocalSensor[FiniteDuration](LSNS_DELTA_TIME)

    /**
     * @return a random double from 0 to 1
     */
    def randomGenerator(): Random = readLocalSensor[Random](LSNS_RANDOM)

    /**
     * @return a random double from 0 to 1
     */
    def nextRandom(): Double = randomGenerator().nextDouble()
  }

  trait StandardNeighbourhoodSensors extends StandardSensorNames {
    self: NeighbourhoodSensorReader =>

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
    def nbrDelay(): NbrSensorRead[FiniteDuration] = readNbrSensor[FiniteDuration](NBR_DELAY)

    /**
      * Time backward view: how long ago information from neighbors was received.
      * For device's neighbors, it is the time of the computation minus the timestamp on the packet.
      * Dropped packets temporarily increase nbrLag.
      * For the current device, it is like deltaTime().
      */
    def nbrLag(): NbrSensorRead[FiniteDuration] = readNbrSensor[FiniteDuration](NBR_LAG)

    /**
      * Get the distance between the current device and its neighbors.
      */
    def nbrRange(): NbrSensorRead[D] = readNbrSensor[D](NBR_RANGE)

    /**
      * Get the direction vectors towards neighbours.
      * @return a point of type P, assuming the currently executing device is the origin
      */
    def nbrVector(): NbrSensorRead[P] = readNbrSensor[P](NBR_VECTOR)
  }
}
