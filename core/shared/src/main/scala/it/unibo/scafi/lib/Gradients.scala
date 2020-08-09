/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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
    val ClassicHopGradient = Gradient((src,metric) => hopGradient(src), false, () => 1)

    def classicGradient(source: Boolean, metric: () => Double = nbrRange): Double =
      rep(Double.PositiveInfinity){ case d =>
        mux(source){ 0.0 }{ minHoodPlus(nbr(d) + metric()) }
      }

    def hopGradient(src: Boolean): Long =
      classicGradient(src, () => 1).toLong
  }

}
