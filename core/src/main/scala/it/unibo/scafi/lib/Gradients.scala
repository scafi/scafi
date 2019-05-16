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

trait StdLib_Gradients {
  self: StandardLibrary.Subcomponent =>

  type Metric = ()=>Double

  trait Gradients {
    self: FieldCalculusSyntax with StandardSensors =>

    case class Gradient(algorithm: (Boolean,()=>Double)=>Double, source: Boolean=false, metric: Metric = nbrRange) {
      def from(s: Boolean): Gradient = this.copy(source = s)
      def withMetric(m: Metric): Gradient = this.copy(metric = m)
      def run(): Double = algorithm(source, metric)
    }

    val ClassicGradient = Gradient(classicGradient(_,_), false, nbrRange)

    def classicGradient(source: Boolean, metric: () => Double = nbrRange): Double =
      rep(Double.PositiveInfinity){ case d =>
        mux(source){ 0.0 }{ minHoodPlus(nbr(d) + metric()) }
      }
  }

}
