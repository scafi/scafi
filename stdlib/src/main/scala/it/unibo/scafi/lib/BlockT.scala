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

package it.unibo.scafi.lib

import scala.concurrent.duration.Duration

trait Stdlib_BlockT {
  self: StandardLibrary.Subcomponent =>

  trait BlockT {
    self: AggregateProgram =>

    def T[V](initial: V, floor: V, decay: V => V)
            (implicit ev: Numeric[V]): V = {
      rep(initial) { v =>
        ev.min(initial, ev.max(floor, decay(v)))
      }
    }

    def T[V](initial: V, decay: V => V)
            (implicit ev: Numeric[V]): V = {
      T(initial, ev.zero, decay)
    }

    def T[V](initial: V)
            (implicit ev: Numeric[V]): V = {
      T(initial, (t: V) => ev.minus(t, ev.one))
    }

    def timer[V](length: V)
                (implicit ev: Numeric[V]) =
      T[V](length)

    def limitedMemory[V, T](value: V, expValue: V, timeout: T)
                           (implicit ev: Numeric[T]) = {
      val t = timer[T](timeout)
      (if (ev.gt(t, ev.zero)) value else expValue, t)
    }

    def timer(dur: Duration): Long = {
      val ct = System.nanoTime()
      // Current time
      val et = ct + dur.toNanos // Time-to-expire (bootstrap)

      rep((et, dur.toNanos)) { case (expTime, remaining) =>
        if (remaining <= 0) (et, 0)
        else (expTime, expTime - ct)
      }._2 // Selects the component expressing remaining time
    }

    def recentlyTrue(dur: Duration, cond: => Boolean): Boolean =
      rep(false) { happened =>
        branch(cond) {
          true
        } {
          branch(!happened) {
            false
          } {
            timer(dur) > 0
          }
        }
      }
  }
}
