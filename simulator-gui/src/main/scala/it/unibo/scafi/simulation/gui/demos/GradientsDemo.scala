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

package it.unibo.scafi.simulation.gui.demos

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, Builtins, FieldUtils, GenericUtils, ID, StandardSensors, TimeUtils}
import it.unibo.scafi.simulation.gui.demos.DoubleUtils.Precision
import it.unibo.scafi.simulation.gui.launcher.scalaFX.Launcher
import it.unibo.scafi.space.Point3D

import scala.concurrent.duration.FiniteDuration

object GradientsDemo extends App{
  import Launcher._
  program = classOf[ClassicGradient]
  nodes = 50
  maxPoint = 1000
  radius = 5
  neighbourRender = false
  launch()
}

class GradientWithObstacle extends AggregateProgram with SensorDefinitions with Gradients {
  def main = g2(sense1, sense2)

  def g1(isSrc: Boolean, isObstacle: Boolean): Double = mux(isObstacle){
    () => aggregate { Double.PositiveInfinity }
  }{
    () => aggregate { classic(isSrc) }
  }()

  def g2(isSrc: Boolean, isObstacle: Boolean): Double = branch(isObstacle){
    Double.PositiveInfinity
  }{
    classic(isSrc)
  }
}

class SteeringProgram extends AggregateProgram with SensorDefinitions {
  def main = steering(sense1)

  def steering(source: Boolean): Point3D = {
    val g = classic(source)
    val p = currentPosition()
    val q = minHoodPLoc((g, mid, p))(nbr{ (g, mid, p) })._3
    Point3D(q.x - p.x, q.y - p.y, q.z - p.z)
  }

  def classic(source: Boolean): Double = rep(Double.PositiveInfinity){ distance =>
    mux(source){ 0.0 }{
      minHoodPlus(nbr{distance} + nbrRange)
    }
  }

  def minHoodPLoc[A](default: A)(expr: => A)(implicit poglb: Ordering[A]): A = {
    import scala.math.Ordered.orderingToOrdered
    val ordering = implicitly[Ordering[A]]
    foldhoodPlus[A](default)((x, y) => if(x <= y) x else y){expr}
  }

  implicit def tupleOrd[A:Ordering, B:Ordering, C]: Ordering[(A,B,C)] = new Ordering[(A,B,C)] {
    import scala.math.Ordered.orderingToOrdered
    override def compare(x: (A, B, C), y: (A, B, C)): Int = (x._1,x._2).compareTo((y._1,y._2))
  }
}

class ShortestPathProgram extends AggregateProgram with Gradients with SensorDefinitions {
  def main = {
    val g = classic(sense1)
    ShortestPath(sense2, g)
  }
}

class CheckSpeed extends AggregateProgram with Gradients with BlockG with SensorDefinitions with GenericUtils {
  implicit val deftime = new Builtins.Defaultable[LocalDateTime] {
    override def default: LocalDateTime = LocalDateTime.now()
  }

  override def main(): Any =  {
    val communicationRadius = 0.2 * 100
    val frequency = 1000000

    val meanTimeToReach = meanCounter(
      ChronoUnit.MILLIS.between(G_v2(sense1, currentTime(), (x: LocalDateTime)=>x, nbrRange()), currentTime()),
      frequency).toLong
    val distance = G[Double](sense1, 0, _ + nbrRange(), nbrRange())
    val speed = distance / meanTimeToReach
    val meanFireInterval = meanCounter(deltaTime().toMillis, frequency)
    val expectedSpeed = communicationRadius / meanFireInterval
    //val estSinglePathSpeed = communicationRadius / (3 * meanFireInterval)
    f"${meanTimeToReach}ms; ${distance}%.1f; $speed%.3f; $expectedSpeed%.3f"
  }
}

class GradientComparison extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = f"${gradientBIS(sense1)}%.1f|${crf(sense1)}%.1f|${classic(sense1)}%.1f"
}

class BISGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = gradientBIS(sense1)
}

class SVDGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = gradientSVD(sense1)
}
class CrfGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = crf(sense1)
}

class BasicGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = gradient(sense1)
}

class ClassicGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = classic(sense1)
}

class ClassicGradientWithG extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = classicWithG(sense1)
}

class ClassicGradientWithGv2 extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = classicWithGv2(sense1)
}

class ClassicGradientWithUnboundedG extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = classicWithUnboundedG(sense1)
}

class DistanceBetween extends AggregateProgram with SensorDefinitions with BlockG {
  def isSource: Boolean = sense1
  def isTarget: Boolean = sense2

  override def main(): Any = distanceBetween(isSource, isTarget)
}

object DoubleUtils {
  case class Precision(p:Double)
  implicit class DoubleWithAlmostEquals(val d:Double) extends AnyVal {
    def ~=(d2:Double)(implicit p:Precision) = (d - d2).abs < p.p
  }
}

trait Gradients extends BlockG
  with FieldUtils
  with TimeUtils
  with GenericUtils { self: AggregateProgram with SensorDefinitions with StandardSensors =>


  def ShortestPath(source: Boolean, gradient: Double): Boolean =
    rep(false)(
      path => mux(source){
        true
      } {
        foldhood(false)(_||_){
          nbr(path) & (gradient == nbr(minHood(nbr(gradient))))
        }
      }
    )

  /*###########################
  ############ CRF ############
  #############################*/

  def crf(source: Boolean, raisingSpeed: Double = 5): Double = rep((Double.PositiveInfinity, 0.0)){ case (g, speed) =>
    mux(source){ (0.0, 0.0) }{
      implicit def durationToDouble(fd: FiniteDuration): Double = fd.toMillis.toDouble / 1000.0
      case class Constraint(nbr: ID, gradient: Double, nbrDistance: Double)

      val constraints = foldhoodPlus[List[Constraint]](List.empty)(_ ++ _){
        val (nbrg, d) = (nbr{g}, nbrRange)
        mux(nbrg + d + speed * (nbrLag()) <= g){ List(Constraint(nbr{mid()}, nbrg, d)) }{ List() }
      }

      if(constraints.isEmpty){
        (g + raisingSpeed * deltaTime(), raisingSpeed)
      } else {
        (constraints.map(c => c.gradient + c.nbrDistance).min, 0.0)
      }
    }
  }._1

  /*#################################
  ############ UTILITIES ############
  #################################*/

  def timeLastChange(expr: => Double): FiniteDuration = {
    import scala.concurrent.duration.DurationLong

    (-rep(timestamp(), expr){ case (tLastChange, lastVal) =>
      val newValue = expr
      import DoubleUtils._; implicit val prec = Precision(0.001)
      (if(newValue ~= lastVal) tLastChange else timestamp(), newValue)
    }._1 + timestamp()).millis
  }

  def damping(oldVal: Double, newVal: Double, delta: Double, factor: Double): Double = {
    if (oldVal > factor * newVal || newVal > factor * oldVal) {
      newVal
    } else {
      val sign = if(oldVal < newVal) 0.5 else -0.5
      val diff = Math.abs(newVal - oldVal)
      if(diff > delta) newVal - delta*sign else oldVal
    }
  }

  /*#################################
    ############ CLASSIC ############
    #################################*/

  def classic(source: Boolean): Double = rep(Double.PositiveInfinity){ distance =>
    mux(source){ 0.0 }{
      // NB: must be minHoodPlus (i.e., not the minHood which includes the device itself)
      //     otherwise a source which stops being a source will continue to count as 0 because of self-messages.
      minHoodPlus(nbr{distance} + nbrRange)
    }
  }

  def classicWithG(source: Boolean): Double = G(source, if(source) 0.0 else Double.PositiveInfinity, (_:Double) + nbrRange, nbrRange)

  def classicWithGv2(source: Boolean): Double = {
    implicit val defValue = Builtins.Defaultable.apply(Double.PositiveInfinity)
    G_v2(source, 0.0, (_:Double) + nbrRange, nbrRange)
  }

  def classicWithUnboundedG(source: Boolean): Double =
    unboundedG3[Double](source, if(source) 0.0 else Double.PositiveInfinity, (_:Double) + nbrRange, nbrRange)

  def classicWithUnboundedG2(source: Boolean): Double =
    unboundedG[Double](source, if(source) 0.0 else Double.PositiveInfinity, (_:Double) + nbrRange, nbrRange, Math.min(_:Double, _:Double))

  def G_v2[V : Builtins.Defaultable](source: Boolean, field: V, acc: V => V, metric: => Double): V =
    rep((Double.MaxValue, field)) { case (dist, value) =>
      mux(source) {
        (0.0, field)
      } {
        import Builtins.Bounded.tupleOnFirstBounded
        minHoodPlus { (nbr {dist} + metric, acc(nbr {value})) }
      }
    }._2


  def unboundedG2[V](source: Boolean, field: V, acc: V=>V, metric: => Double)
                    (implicit idOrd: Ordering[ID]): V = {
    rep(Double.PositiveInfinity, field) { case (dist, value) =>
      mux(source) {
        (0.0, field)
      } {
        val res = foldhoodPlus((Double.PositiveInfinity, mid, field)) { case (d1 @ (g1: Double, id1: ID, v1: V), d2 @ (g2: Double, id2: ID, v2: V)) =>
          import scala.math.Ordered.orderingToOrdered
          if((g1,id1) <= (g2,id2)) d1 else d2
        } { (nbr{ dist } + metric, nbr{ mid }, acc(nbr{ value })) }
        (res._1, res._3)
      }
    }._2
  }

  def minHoodPLoc[A](default: A)(expr: => A)(implicit poglb: Ordering[A]): A = {
    import scala.math.Ordered.orderingToOrdered
    val ordering = implicitly[Ordering[A]]
    foldhoodPlus[A](default)((x, y) => if(x <= y) x else y){expr}
  }

  implicit def tupleOrd[A:Ordering, B:Ordering, C]: Ordering[(A,B,C)] = new Ordering[(A,B,C)] {
    import scala.math.Ordered.orderingToOrdered
    override def compare(x: (A, B, C), y: (A, B, C)): Int = (x._1,x._2).compareTo((y._1,y._2))
  }

  def unboundedG3[V](source: Boolean, field: V, acc: V=>V, metric: => Double)
                    (implicit idOrd: Ordering[ID]): V = {
    rep(Double.PositiveInfinity, mid, field) { case (dist, _, value) =>
      mux(source) {
        (0.0, mid, field)
      } {
        minHoodPLoc((Double.PositiveInfinity, mid, field)){ (nbr{ dist } + metric, nbr{ mid }, acc(nbr{ value })) }
      }
    }._3
  }

  def unboundedG[V](source: Boolean, field: V, acc: V=>V, metric: => Double, aggr: (V,V) => V): V = {
    rep(Double.PositiveInfinity, field) { case (dist, value) =>
      mux(source) {
        (0.0, field)
      } {
        foldhoodPlus((Double.PositiveInfinity, field)) { case (data1 @ (d1: Double, v1: V), data2 @ (d2: Double, v2: V)) =>
          import DoubleUtils.DoubleWithAlmostEquals
          implicit val prec = Precision(0.00001)
          if(d1 ~= d2) ((d1+d2)/2, aggr(v1, v2))
          else if (d1 < d2) data1 else data2
        } { (nbr {dist} + metric, acc(nbr {value})) }
      }
    }._2
  }

  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){ distance =>
      mux(source) {
        0.0
      }{
        foldhoodPlus(Double.PositiveInfinity)(Math.min(_,_)){ nbr{distance} + nbrRange }
      }
    }

  /*############################
  ############ SVD ############
  #############################*/

  def gradientSVD(source: Boolean, metric: => Double = nbrRange(), lagMetric: => Double = nbrLag().toMillis): Double = {
    val defaultDist = if(source) 0.0 else Double.PositiveInfinity
    val loc = (defaultDist, defaultDist, mid(), false)
    // REP tuple: (spatial distance estimate, temporal distance estimate, source ID, obsolete value detected flag)
    rep[(Double,Double,Int,Boolean)](loc) {
      case old @ (spaceDistEst, timeDistEst, sourceId, isObsolete) => {
        // (1) Let's calculate new values for spaceDistEst and sourceId
        import Builtins.Bounded._
        val (newSpaceDistEst, newSourceId) = minHood {
          mux(nbr{isObsolete} && excludingSelf.anyHood { !nbr{isObsolete} })
          { // let's discard neighbours where 'obsolete' flag is true
            // (unless 'obsolete' flag is true for all the neighbours)
            (defaultDist, mid())
          } {
            // if info is not obsolete OR all nbrs have obsolete info
            // let's use classic gradient calculation
            (nbr{spaceDistEst} + metric, nbr{sourceId})
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
              nbr{isObsolete} && nbr{sourceId} == newSourceId && nbr{timeDistEst}+lagMetric < newTimeDistEst + 0.0001
            }

        List[(Double,Double,Int,Boolean)]((newSpaceDistEst, newTimeDistEst, newSourceId, newObsolete), loc).min
      }
    }._1 // Selects estimated distance
  }

  /**
    * At the heart of SVD algorithm. This function is responsible to kick-start the reconfiguration process.
    * @param time
    * @return
    */
  def detect(time: Double): Boolean = {
    // Let's keep track into repCount of how much time is elapsed since the first time
    // the current info (originated from the source in time 'time') reached the current device
    val repCount = rep(0.0) { old =>
      if(Math.abs(time - delay(time)) < 0.0001) { old + deltaTime().toMillis } else { 0.0 }
    }

    val obsolete = repCount > rep[(Double, Double, Double)](2, 8, 16) { case (avg, sqa, bound) =>
      // Estimate of the average peak value for repCount, obtained by exponentially filtering
      // with a factor 0.1 the peak values of repCount
      val newAvg = 0.9 * avg + 0.1 * delay(repCount)
      // Estimate of the average square of repCount peak values
      val newSqa = 0.9 * sqa + 0.1 * Math.pow(delay(repCount), 2)
      // Standard deviation
      val stdev = Math.sqrt(newSqa - Math.pow(newAvg, 2))
      // New bound
      val newBound = newAvg + 7*stdev
      (newAvg, newSqa, newBound)
    }._3

    obsolete
  }

  /*############################
  ############ BIS ############
  #############################*/

  def gradientBIS(source: Boolean): Double = {
    val avgFireInterval = meanCounter(deltaTime().toMillis, 1000000)
    val speed = 1.0 / avgFireInterval
    val commRadius = 0.2

    rep((Double.PositiveInfinity, Double.PositiveInfinity)){ case (spatialDist: Double, tempDist: Double)  =>
      mux(source){ (0.0, 0.0) }{
        minHoodPlus {
          val newEstimate = Math.max(nbr{spatialDist} + nbrRange(), speed * nbr{tempDist} - commRadius)
          (newEstimate, nbr{tempDist} + nbrLag.toMillis/1000.0)
        }
      }
    }._1
  }
}