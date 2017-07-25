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

trait Stdlib_BlocksWithGC {
  self: StandardLibrary.Subcomponent =>

  import Builtins._

  trait BlocksWithGC {
    self: BlockC with BlockG =>

    def summarize(sink: Boolean, acc: (Double, Double) => Double, local: Double, Null: Double): Double =
      broadcast(sink, C(distanceTo(sink), acc, local, Null))

    def average(sink: Boolean, value: Double): Double =
      summarize(sink, (a, b) => {
        a + b
      }, value, 0.0) / summarize(sink, (a, b) => a + b, 1, 0.0)
  }
}
