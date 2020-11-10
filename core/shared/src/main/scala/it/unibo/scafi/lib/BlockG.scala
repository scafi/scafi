/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BlockG {
  self: StandardLibrary.Subcomponent =>

  // scalastyle:off method.name

  import it.unibo.scafi.languages.TypesInfo._

  trait BlockGInterface extends SimpleGradientsInterface with GenericUtils with StateManagement {
    self: ScafiBaseLanguage with StandardSensors with NeighbourhoodSensorReader with LanguageDependant =>

    /**
      * Version of G (Gradient-Cast) that takes a Gradient algorithm as input.
      * @param gradient The gradient algorithm to use -- notice that this encapsulates details about the gradient field (e.g., the sources)
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
      */
    def G_along[V](g: Double, metric: Metric, field: V, acc: V => V): V =
      G_along_withAccumulator[V](g, metric, field, simpleAccFromLowestPotential(_, _, _, _, acc))

    private def G_along_withAccumulator[V](g: Double, metric: Metric, field: V, accumulator: (Double, V, Metric, V) => V): V = {
      rep(field) { case (value) =>
        mux(g==0.0) {
          field
        } {
          accumulator(g, value, metric, field)
        }
      }
    }

    def G[V](source: Boolean, field: V, acc: V => V, metric: Metric): V =
      Gg[V](ClassicGradient.from(source).withMetric(metric), field, acc)

    def G_metricAccumulator(source: Boolean, field: Double, accMetric: Metric, distanceMetric: Metric): Double = {
      G_along_withAccumulator[Double](
        ClassicGradient.from(source).withMetric(distanceMetric).run(),
        distanceMetric,
        field,
        metricAccFromLowestPotential(_, _, _, _, accMetric)
      )
    }

    /**
      * Curried version of [[G]]
      */
    def Gcurried[V](source: Boolean)(field: V)(acc: V => V)(metric: Metric = nbrRange): V =
      G(source, field, acc, metric)

    /**
      * A field of distance (i.e., a gradient) from a `source`, based on a given `metric`
      */
    def distanceTo(source: Boolean, metric: Metric = nbrRange): Double =
      G_metricAccumulator(source, mux(source){0.0}{Double.PositiveInfinity}, metric, nbrRange)

    /**
      * Hop distance
      */
    def hopDistance(source: Boolean): Double = distanceTo(source, () => constantRead(1))

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
      !(ds + dt == Double.PositiveInfinity && db == Double.PositiveInfinity) && ds + dt <= db + width
    }

    private[StdLib_BlockG] def simpleAccFromLowestPotential[V](g: Double, value: V, metric: Metric, field: V, acc: V => V): V
    private[StdLib_BlockG] def metricAccFromLowestPotential(g: Double, value: Double, metric: Metric, field: Double, accMetric: Metric): Double
  }

  trait BlockG_ScafiStandard extends BlockGInterface with FieldUtils with SimpleGradients_ScafiStandard {
    self: ScafiStandardLanguage with StandardSensors =>

    override private[StdLib_BlockG] def simpleAccFromLowestPotential[V](g: Double, value: V, metric: Metric, field: V, acc: V => V): V =
      excludingSelf
        .minHoodSelector[Double,V](nbr{g} + metric())(acc(nbr{value}))
        .getOrElse(field)

    override private[StdLib_BlockG] def metricAccFromLowestPotential(g: Double, value: Double, metric: Metric, field: Double, accMetric: Metric): Double =
      simpleAccFromLowestPotential[Double](g, value, metric, field, _ + accMetric())
  }

  trait BlockG_ScafiFC extends BlockGInterface with SimpleGradients_ScafiFC {
    self: ScafiFCLanguage with StandardSensors =>

    override private[StdLib_BlockG] def simpleAccFromLowestPotential[V](g: Double, value: V, metric: Metric, field: V, acc: V => V): V =
      doAcc[V](g, value, metric, field, _.map(acc))

    override private[StdLib_BlockG] def metricAccFromLowestPotential(g: Double, value: Double, metric: Metric, field: Double, accMetric: Metric): Double =
      doAcc[Double](g, value, metric, field, _ + accMetric())

    private[this] def doAcc[V](g: Double, value: V, metric: Metric, field: V, fieldAcc: Field[V] => Field[V]): V =
      (metric() + nbrField(g)).zip(fieldAcc(nbrField(value))).withoutSelf.fold((Double.MaxValue, field)){case (a, b) =>
        if (b._1 < a._1)
          b
        else
          a
      }._2
  }
}
