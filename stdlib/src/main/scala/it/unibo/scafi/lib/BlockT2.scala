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

trait Stdlib_BlockT2 {
  self: StandardLibrary.Subcomponent =>

  trait BlockT2 {
    self: AggregateProgram =>

    def implicitMin[V: Numeric](a: V, b: V): V = implicitly[Numeric[V]].min(a, b)

    def implicitMax[V: Numeric](a: V, b: V): V = implicitly[Numeric[V]].max(a, b)

    def T[V: Numeric](initial: V)(floor: V)(decay: V => V): V = {
      rep(initial) { v => implicitMin(initial, implicitMax(floor, decay(v))) }
    }

    def linearFlow(time: Double): Double =
      T(time)(0.0)(v => v - 1)

    def timer(time: Double): Boolean =
      linearFlow(time) == 0.0
  }
}
