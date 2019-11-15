/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BlockG {
  self: StandardLibrary.Subcomponent =>

  // scalastyle:off method.name

  import Builtins._

  trait BlockG extends Gradients with FieldUtils {
    self: FieldCalculusSyntax with StandardSensors =>

    def Gg[V](gradient: Gradient, field: V, acc: V => V): V = {
      val g = gradient.run()
      rep(field) { case (value) =>
        mux(g==0.0){ field }{ excludingSelf.minHoodSelector[Double,V](nbr{g} + gradient.metric())(acc(nbr{value})).getOrElse(field) }
      }
    }

    def G_along[V](g: Double, metric: Metric, field: V, acc: V => V): V = {
      rep(field) { case (value) =>
        mux(g==0.0){ field }{ excludingSelf.minHoodSelector[Double,V](nbr{g} + metric())(acc(nbr{value})).getOrElse(field) }
      }
    }

    def G[V](source: Boolean, field: V, acc: V => V, metric: Metric): V =
      Gg[V](ClassicGradient.from(source).withMetric(metric), field, acc)

    def G2[V](source: Boolean)(field: V)(acc: V => V)(metric: Metric = nbrRange): V =
      G(source, field, acc, metric)

    def distanceTo(source: Boolean, metric: Metric = nbrRange): Double =
      G2(source)(mux(source){0.0}{Double.PositiveInfinity})(_ + metric())()

    def broadcast[V](source: Boolean, field: V, metric: Metric = nbrRange): V =
      G2(source)(field)(v => v)(metric)

    def distanceBetween(source: Boolean, target: Boolean, metric: Metric = nbrRange): Double =
      broadcast(source, distanceTo(target, metric), metric)

    def channel(source: Boolean, target: Boolean, width: Double): Boolean = {
      val ds = distanceTo(source)
      val dt = distanceTo(target)
      val db = distanceBetween(source, target)
      !(ds + dt == Double.PositiveInfinity && db == Double.PositiveInfinity) && ds + dt <= db + width
    }
  }

}
