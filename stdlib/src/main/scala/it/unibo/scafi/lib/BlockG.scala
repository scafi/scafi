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

trait Stdlib_BlockG {
  self: StandardLibrary.Subcomponent =>

  // scalastyle:off method.name

  import Builtins._

  trait BlockG {
    self: FieldCalculusSyntax with StandardSensors =>

    def G[V: Bounded](source: Boolean, field: V, acc: V => V, metric: => Double): V =
      rep((Double.MaxValue, field)) { case (dist, value) =>
        mux(source) {
          (0.0, field)
        } {
          minHoodPlus {
            (nbr { dist } + metric, acc(nbr { value }))
          }
        }
      }._2

    def G3[V: PartialOrderingWithGLB](source: Boolean, field: V, acc: V => V, metric: => Double): V =
      rep((Double.MaxValue, field)) { case (dist, value) =>
        mux(source) {
          (0.0, field)
        } {
          import PartialOrderingWithGLB._
          minHoodPlusLoc[(Double,V)]((Double.PositiveInfinity, field)) {
            (nbr { dist } + metric, acc(nbr { value }))
          } (poglbTuple(pogldouble, implicitly[PartialOrderingWithGLB[V]]))
        }
      }._2

    def G2[V: Bounded](source: Boolean)(field: V)(acc: V => V)(metric: => Double = nbrRange): V =
      G(source, field, acc, metric)

    def distanceTo(source: Boolean): Double =
      G2(source)(0.0)(_ + nbrRange)()

    def broadcast[V: Bounded](source: Boolean, field: V): V =
      G2(source)(field)(v => v)()

    def distanceBetween(source: Boolean, target: Boolean): Double =
      broadcast(source, distanceTo(target))
  }

}
