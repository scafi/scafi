/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.math.Numeric.DoubleIsFractional
import it.unibo.utils.Filters.expFilter

import java.util.concurrent.TimeUnit

trait StdLibGradients {
  self: StandardLibrary.Subcomponent =>

  type Metric = ()=>Double

  import Builtins.Bounded

  implicit val idBounded: Bounded[ID]

  val DEFAULT_CRF_RAISING_SPEED: Double = 5
  val DEFAULT_FLEX_CHANGE_TOLERANCE_EPSILON: Double = 0.5
  val DEFAULT_FLEX_DELTA: Double = 0.5
  val DEFAULT_ULT_FACTOR: Double = 0.1

  trait Gradients extends GenericUtils with StateManagement {
    self: FieldCalculusSyntax with StandardSensors with BlockG =>

    final case class Gradient(algorithm: (Boolean, () => Double) => Double, source: Boolean = false, metric: Metric = nbrRange) {
      def from(s: Boolean): Gradient = this.copy(source = s)

      def withMetric(m: Metric): Gradient = this.copy(metric = m)

      def run(): Double = algorithm(source, metric)
    }

    val ClassicGradient: Gradient =
      Gradient(classicGradient, source = false, nbrRange)
    val ClassicHopGradient: Gradient =
      Gradient((src, _) => hopGradient(src), source = false, () => 1)

    def bisGradientBuilder(commRadius: Double,
                           lagMetric: => Double = nbrLag().toUnit(TimeUnit.MILLISECONDS)): Gradient =
      Gradient(bisGradient(commRadius, lagMetric), source = false, nbrRange)

    def crfGradientBuilder(raisingSpeed: Double = DEFAULT_CRF_RAISING_SPEED,
                           lagMetric: => Double = nbrLag().toUnit(TimeUnit.MILLISECONDS)): Gradient =
      Gradient(crfGradient(raisingSpeed, lagMetric), source = false, nbrRange)

    def flexGradientBuilder(communicationRadius: Double, epsilon: Double = DEFAULT_FLEX_CHANGE_TOLERANCE_EPSILON, delta: Double = DEFAULT_FLEX_DELTA): Gradient =
      Gradient(flexGradient(epsilon, delta, communicationRadius), source = false, nbrRange)

    def svdGradientBuilder(lagMetric: => Double = nbrLag().toUnit(TimeUnit.MILLISECONDS)): Gradient =
      Gradient(svdGradient(lagMetric), source = false, nbrRange)

    def ultGradientBuilder(radius: Double, factor: Double = DEFAULT_ULT_FACTOR): Gradient =
      Gradient(ultGradient(radius, factor), source = false, nbrRange)

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
        hops => mux(source) {0.0} {1 + minHood(nbr {hops})
        }
      }

    def bisGradient(commRadius: Double,
                    lagMetric: => Double = nbrLag().toMillis)
                   (source: Boolean,
                    metric: Metric = nbrRange
                   ): Double = {
      val avgFireInterval = meanCounter(deltaTime().toUnit(TimeUnit.MILLISECONDS), 1.second.toMicros)
      val speed = DEFAULT_FLEX_DELTA / avgFireInterval
      rep((Double.PositiveInfinity, Double.PositiveInfinity)) { case (spatialDist: Double, tempDist: Double) =>
        mux(source) {
          (0.0, 0.0)
        } {
          minHoodPlus {
            val newEstimate = Math.max(nbr {
              spatialDist
            } + metric(), speed * nbr {
              tempDist
            } - commRadius)
            (newEstimate, nbr {
              tempDist
            } + lagMetric / 1000.0)
          }
        }
      }._1
    }

    def crfGradient(raisingSpeed: Double = DEFAULT_CRF_RAISING_SPEED,
                    lagMetric: => Double = nbrLag().toMillis)
                   (source: Boolean,
                     metric: Metric = nbrRange
                   ): Double =
      rep((Double.PositiveInfinity, 0.0)) {
        case (g, speed) =>
          mux(source){ (0.0, 0.0) }{
            implicit def durationToDouble(fd: FiniteDuration): Double = fd.toMillis.toDouble / 1000.0
            final case class Constraint(nbr: ID, gradient: Double, nbrDistance: Double)

            val constraints = foldhoodPlus[List[Constraint]](List.empty)(_ ++ _){
              val (nbrg, d) = (nbr{g}, metric())
              mux(nbrg + d + speed * lagMetric <= g){ List(Constraint(nbr{mid()}, nbrg, d)) }{ List() }
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
    def flexGradient(epsilon: Double = DEFAULT_FLEX_CHANGE_TOLERANCE_EPSILON,
                     delta: Double = DEFAULT_FLEX_DELTA,
                     communicationRadius: Double = DEFAULT_FLEX_DELTA)
                    (source: Boolean,
                     metric: Metric = nbrRange
                    ): Double =
      rep(Double.PositiveInfinity){ g =>
        import Builtins.Bounded._ // for min/maximizing over tuples
        def distance = Math.max(nbrRange(), delta * communicationRadius)
        val maxLocalSlope: (Double,ID,Double,Double) =
          maxHood { ((g - nbr{g})/distance, nbr{mid}, nbr{g}, metric()) }
        val constraint = minHoodPlus{ (nbr{g} + distance) }
        mux(source){ 0.0 }{
          if(Math.max(communicationRadius, 2*constraint) < g) {
            constraint
          }
          else if(maxLocalSlope._1 > 1 + epsilon) {
            maxLocalSlope._3 + (1 + epsilon) * Math.max(delta * communicationRadius, maxLocalSlope._4)
          }
          else if(maxLocalSlope._1 < 1 - epsilon){
            maxLocalSlope._3 + (1 - epsilon) * Math.max(delta * communicationRadius, maxLocalSlope._4)
          } else {
            g
          }
        }
      }

    // scalastyle:off
    def svdGradient(lagMetric: => Double = nbrLag().toMillis)(
                     source: Boolean,
                     metric: Metric = nbrRange): Double = {
      /**
        * At the heart of SVD algorithm. This function is responsible to kick-start the reconfiguration process.
        */
      def detect(time: Double, threshold: Double = 0.0001): Boolean = {
        // Let's keep track into repCount of how much time is elapsed since the first time
        // the current info (originated from the source in time 'time') reached the current device
        val repCount = rep(0.0) { old =>
          if (Math.abs(time - delay(time)) < threshold) {
            old + deltaTime().toMillis
          } else { 0.0 }
        }
        val obsolete = repCount > rep[(Double, Double, Double)](2, 8, 16) { case (avg, sqa, _) =>
          // Estimate of the average peak value for repCount, obtained by exponentially filtering
          // with a factor 0.1 the peak values of repCount
          val newAvg = 0.9 * avg + DEFAULT_ULT_FACTOR * delay(repCount)
          // Estimate of the average square of repCount peak values
          val newSqa = 0.9 * sqa + DEFAULT_ULT_FACTOR * Math.pow(delay(repCount), 2)
          // Standard deviation
          val stdev = Math.sqrt(newSqa - Math.pow(newAvg, 2))
          // New bound
          val newBound = newAvg + 7 * stdev
          (newAvg, newSqa, newBound)
        }._3
        obsolete
      }

      val defaultDist = if(source) 0.0 else Double.PositiveInfinity
      val loc = (defaultDist, defaultDist, mid(), false)
      // REP tuple: (spatial distance estimate, temporal distance estimate, source ID, obsolete value detected flag)
      rep[(Double,Double,ID,Boolean)](loc) {
        case old @ (spaceDistEst, timeDistEst, sourceId, isObsolete) => {
          // (1) Let's calculate new values for spaceDistEst and sourceId
          import Builtins.Bounded._
          val (newSpaceDistEst: Double, newSourceId: ID @unchecked) =
          minHood {
            mux(nbr{isObsolete} && excludingSelf.anyHood { !nbr{isObsolete} })
            { // let's discard neighbours where 'obsolete' flag is true
              // (unless 'obsolete' flag is true for all the neighbours)
              (defaultDist, mid())
            } {
              // if info is not obsolete OR all nbrs have obsolete info
              // let's use classic gradient calculation
              (nbr{spaceDistEst} + metric(), nbr{sourceId})
            }
          }

          // (2) The most recent timeDistEst for the newSourceId is retrieved
          // by minimising nbrs' values for timeDistEst + their relative time distance
          // (we only consider neighbours that have same value for 'sourceId')
          val newTimeDistEst = minHood{
            mux(nbr{sourceId} != newSourceId){
              // let's discard neighbours with a sourceId different than newSourceId
              defaultDist
            } {
              nbr { timeDistEst } + lagMetric
            }
          }

          // (3) Let's compute if the newly produced info is to be considered obsolete
          val loop = newSourceId == mid() && newSpaceDistEst < defaultDist
          val newObsolete =
            detect(timestamp() - newTimeDistEst) || // (i) if the time when currently used info started
              //     from sourceId is too old to be reliable
              loop || // or, (ii) if the device's value happens to be calculated from itself,
              excludingSelf.anyHood { // or, (iii) if any (not temporally farther) nbr with same sourceId  than
                //           the device's one has already been claimed obsolete
                nbr{isObsolete} && nbr{sourceId} == newSourceId && nbr{timeDistEst} + lagMetric < newTimeDistEst + 0.0001
              }

          //List[(Double,Double,ID,Boolean)]((newSpaceDistEst, newTimeDistEst, newSourceId, newObsolete), loc).min
          if (newSpaceDistEst >= loc._1) {
            if (newTimeDistEst >= loc._2)  {
              if (newObsolete >= loc._4) {
                loc
              } else {
                (newSpaceDistEst, newTimeDistEst, newSourceId, newObsolete)
              }
            } else {
              (newSpaceDistEst, newTimeDistEst, newSourceId, newObsolete)
            }
          }  else {
            (newSpaceDistEst, newTimeDistEst, newSourceId, newObsolete)
          }
        }
      }._1 // Selects estimated distance
    }
    // scalastyle:on

    def ultGradient(radius: Double,
                    factor: Double = DEFAULT_ULT_FACTOR)
                   (source: Boolean,
                    metric: Metric = nbrRange): Double = {
      def inertialFilter(value: Double, filterFactor: Double): Double = {
        val dt: Double = deltaTime().toMillis
        val at: Double = expFilter(dt, filterFactor)
        val ad: Double = expFilter(Math.abs(value - delay(value)), filterFactor)
        rep (value) { old => {
          if (!old.isInfinite) {
            val v: Double = Math.signum(old) * Math.min( Math.abs(value - old)/dt, ad/at)
            val r = old + v * dt
            if (r.isNaN) value else r
          } else {
            value
          }
        } }
      }
      inertialFilter(Math.max(
        svdGradientBuilder().from(source).withMetric(metric).run(),
        bisGradientBuilder(radius).from(source).withMetric(metric).run()
      ), factor)
    }
  }
}
