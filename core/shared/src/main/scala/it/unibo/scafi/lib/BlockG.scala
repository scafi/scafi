/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLibBlockG {
  self: StandardLibrary.Subcomponent =>

  // scalastyle:off method.name

  import Builtins._

  trait BlockG extends Gradients with FieldUtils with GenericUtils with StateManagement {
    self: FieldCalculusSyntax with StandardSensors =>

    /**
      * Version of G (Gradient-Cast) that takes a Gradient algorithm as input.
      * This is motivated by the desire of speeding G up by using
      * @param gradient The gradient algorithm to use -- notice that this encapsulates details about the gradient field (e.g., the sources)
      * @deprecated As it seems buggy: see {@link #G_along[V](Double,Metric,V,V=>V) G_along} method.
      */
    def Gg[V](gradient: Gradient, field: V, acc: V => V): V =
      G_along(gradient.run(), gradient.metric, field, acc)

    /**
      * Version of G (Gradient-Cast) that takes a potential field `g` as input
      * @param g potential field
      * @param metric metric to use for the "last step"
      * @param field value of the field for sources
      * @param acc aggregator
      * @tparam V type of the value to be accumulated
      * @return a field that locally provides the value of the gradient-cast (`field` at sources, and an accumulation value along the way)
      * @deprecated As it seems buggy.
      */
    def G_along[V](g: Double, metric: Metric, field: V, acc: V => V): V = {
      rep(field) { case (value) =>
        mux(g==0.0){ field }{ excludingSelf.minHoodSelector[Double,V](nbr{g} + metric())(acc(nbr{value})).getOrElse(field) }
      }
    }

    def G[V](source: Boolean, field: V, acc: V => V, metric: () => Double): V =
      rep((Double.MaxValue, field)) { case (dist, value) =>
        mux(source) {
          (0.0, field)
        } {
          excludingSelf
            .minHoodSelector(nbr { dist } + metric())((nbr { dist } + metric(), acc(nbr { value })))
            .getOrElse((Double.PositiveInfinity, field))
        }
      }._2

    /**
      * Curried version of [[G]]
      */
    def Gcurried[V](source: Boolean)(field: V)(acc: V => V)(metric: Metric = nbrRange): V =
      G(source, field, acc, metric)

    /**
      * A field of distance (i.e., a gradient) from a `source`, based on a given `metric`
      */
    def distanceTo(source: Boolean, metric: Metric = nbrRange): Double =
      Gcurried(source)(mux(source){0.0}{Double.PositiveInfinity})(_ + metric())()

    /**
      * Hop distance
      */
    def hopDistance(source: Boolean): Double = distanceTo(source, () =>1)

    /**
      * Broadcast information outward from a source field.
      * @param source The source of the broadcast
      * @param field Field denoting (1) at the source, the value to be broadcast; and (2) in other places, the default value of the broadcast field.
      * @param metric Metric that parameterises the gradient-based construction of the the propagation structure
      * @tparam V Type of the value to be broadcast
      * @return A broadcast field.
      */
    def broadcast[V](source: Boolean, field: V, metric: Metric = nbrRange): V =
      Gcurried(source)(field)(v => v)(metric)

    def broadcastAlongGradient[V](g: Gradient, field: V): V = {
      val g = g.run()
      excludingSelf.minHoodSelector(nbr { g })(nbr { field }).getOrElse(field)
    }

    /**
      * Distance between `source` to `target` based on `metric`
      */
    def distanceBetween(source: Boolean, target: Boolean, metric: Metric = nbrRange): Double =
      broadcast(source, distanceTo(target, metric), metric)

    /**
      * A channel from a `source` to a `target` with width `width`.
      * A channel is a boolean field which is true in correspondence of the device positioned
      *  along the minimum path from source to target.
      */
    def channel(source: Boolean, target: Boolean, width: Double): Boolean = {
      val ds = distanceTo(source)
      val dt = distanceTo(target)
      val db = distanceBetween(source, target)
      !((ds + dt).isInfinite && db.isInfinite) && ds + dt <= db + width
    }
  }

}
