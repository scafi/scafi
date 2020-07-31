/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

import scala.concurrent.duration.FiniteDuration

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

    def CRFGradient(source: Boolean, raisingSpeed: Double = 5): Double =
      rep((Double.PositiveInfinity, 0.0)) {
        case (g, speed) =>
          mux(source){ (0.0, 0.0) }{
            implicit def durationToDouble(fd: FiniteDuration): Double = fd.toMillis.toDouble / 1000.0
            //TODO: remove Any
            case class Constraint(nbr: Any, gradient: Double, nbrDistance: Double)

            val constraints = foldhoodPlus[List[Constraint]](List.empty)(_ ++ _){
              val (nbrg, d) = (nbr{g}, nbrRange())
              mux(nbrg + d + speed * nbrLag() <= g){ List(Constraint(nbr{mid()}, nbrg, d)) }{ List() }
            }

            if(constraints.isEmpty){
              (g + raisingSpeed * deltaTime(), raisingSpeed)
            } else {
              (constraints.map(c => c.gradient + c.nbrDistance).min, 0.0)
            }
          }
      }._1

    /**
      * Idea: a device should change its estimate only for significant errors.
      * Useful when devices far from the source need only coarse estimates.
      * Flex gradient provides tunable trade-off between precision and communication cost.
      *
      * @param source Source fields of devices from which the gradient is calculated
      * @param epsilon Parameter expressing tolerance wrt changes
      * @param delta Distortion into the distance measure, such that neighbor distance is
      *              never considered to be less than delta * communicationRadius.
      * @param communicationRadius
      * @return
    */
    def FlexGradient(source: Boolean,
             epsilon: Double = 0.5,
             delta: Double = 1.0,
             communicationRadius: Double = 1.0
            ): Double =
      rep(Double.PositiveInfinity){ g =>
        def distance = Math.max(nbrRange(), delta * communicationRadius)

        import Builtins.Bounded._ // for min/maximizing over tuples
        val maxLocalSlope: (Double,ID,Double,Double) = ??? // TODO: typeclass resolution for tuple (Double,ID,Double,Double) broke
        /*maxHood {
          ((g - nbr{g})/distance, nbr{mid}, nbr{g}, nbrRange())
        }*/
        val constraint = minHoodPlus{ (nbr{g} + distance) }

        mux(source){ 0.0 }{
          if(Math.max(communicationRadius, 2*constraint) < g) {
            constraint
          }
          else if(maxLocalSlope._1 > 1 + epsilon) {
            maxLocalSlope._3 + (1 + epsilon)*Math.max(delta * communicationRadius, maxLocalSlope._4)
          }
          else if(maxLocalSlope._1 < 1 - epsilon){
            maxLocalSlope._3 + (1 - epsilon)*Math.max(delta * communicationRadius, maxLocalSlope._4)
          } else {
            g
          }
        }
      }

  }
}
