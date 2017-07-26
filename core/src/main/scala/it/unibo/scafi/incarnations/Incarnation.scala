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

package it.unibo.scafi.incarnations

import it.unibo.scafi.core.{Core, Engine, RichLanguage}
import it.unibo.scafi.platform.SpaceTimeAwarePlatform
import it.unibo.scafi.space.BasicSpatialAbstraction
import it.unibo.scafi.time.TimeAbstraction

import scala.concurrent.duration.FiniteDuration

trait Incarnation extends Core
  with Engine
  with RichLanguage
  with SpaceTimeAwarePlatform
  with BasicSpatialAbstraction
  with TimeAbstraction {

  trait AggregateProgramSpec extends ProgramSchema with Constructs with Builtins

  trait AggregateInterpreter extends ExecutionTemplate with Constructs with Builtins with Serializable {
    type MainResult = Any
  }

  trait AggregateProgram extends AggregateInterpreter

  class BasicAggregateInterpreter extends AggregateInterpreter {
    override def main(): MainResult = ???
  }

  trait StandardSensors {
    self: AggregateProgram =>

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
    def nbrRange(): D = nbrvar[D](NBR_RANGE_NAME)

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
      * @return the duration since the last round of execution
      */
    def deltaTime(): FiniteDuration = sense[FiniteDuration](LSNS_DELTA_TIME)
  }

  class ScafiDSL extends Constructs with Builtins {
    var engine: ExecutionTemplate = _

    def program[T](et: Option[ExecutionTemplate] = None)(expr: =>T): CONTEXT=>EXPORT = {
      engine = et.getOrElse(new ExecutionTemplate {
        override def main() = expr
        override type MainResult = T
      })
      (ctx: CONTEXT) => engine(ctx)
    }

    override def mid(): ID = engine.mid()
    override def aggregate[A](f: => A): A = engine.aggregate(f)
    override def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = engine.foldhood(init)(aggr)(expr)
    override def nbrvar[A](name: NSNS): A = engine.nbrvar(name)
    override def nbr[A](expr: => A): A = engine.nbr(expr)
    override def sense[A](name: LSNS): A = engine.sense(name)
    override def rep[A](init: =>A)(fun: (A) => A): A = engine.rep(init)(fun)
  }
}
