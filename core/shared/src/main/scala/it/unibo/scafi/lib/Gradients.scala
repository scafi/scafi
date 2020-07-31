/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_Gradients {
  self: StandardLibrary.Subcomponent =>

  type Metric = ()=>Double

  trait Gradients {
    self: FieldCalculusSyntax with StandardSensors with GenericUtils =>

    case class Gradient(algorithm: (Boolean, () => Double) => Double, source: Boolean = false, metric: Metric = nbrRange) {
      def from(s: Boolean): Gradient = this.copy(source = s)

      def withMetric(m: Metric): Gradient = this.copy(metric = m)

      def run(): Double = algorithm(source, metric)
    }

    val ClassicGradient: Gradient = Gradient(classicGradient, source = false, nbrRange)

    def classicGradient(source: Boolean, metric: () => Double = nbrRange): Double =
      rep(Double.PositiveInfinity) { case d =>
        mux(source) {
          0.0
        } {
          minHoodPlus(nbr(d) + metric())
        }
      }

    def hopGradient(source: Boolean): Double =
      rep(Double.PositiveInfinity) {
        hops => {
          mux(source) {
            0.0
          } {
            1 + minHood(nbr {
              hops
            })
          }
        }
      }

    def BISGradient(source: Boolean, commRadius: Double = 0.2): Double = {
      //meanCounter returns NaN
      val avgFireInterval = meanCounter(deltaTime().toMillis, 1000000)
      val speed = 1.0 / avgFireInterval

      rep((Double.PositiveInfinity, Double.PositiveInfinity)) { case (spatialDist: Double, tempDist: Double) =>
        mux(source) {
          (0.0, 0.0)
        } {
          minHoodPlus {
            val newEstimate = Math.max(nbr {
              spatialDist
            } + nbrRange(), speed * nbr {
              tempDist
            } - commRadius)
            (newEstimate, nbr {
              tempDist
            } + nbrLag().toMillis / 1000.0)
          }
        }
      }._1
    }
  }
}
