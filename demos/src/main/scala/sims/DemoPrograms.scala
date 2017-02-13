package sims

import java.util.concurrent.TimeUnit

import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation.NBR_RANGE_NAME
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation._
import Builtins.OrderingFoldable
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.ID

import scala.concurrent.duration.Duration
import it.unibo.scafi.simulation.gui.model.AggregateProgram

/**
  * @author Roberto Casadei
  *
  */

class Mid extends AggregateProgram {
  override def main() = mid()
}

class Types extends AggregateProgram {
  override def main() = ("a", 1, List(10,20))
}

class CountRounds extends AggregateProgram {
  override def main() = rep(0)(x => x + 1)
}

class CountNeighbours extends AggregateProgram {
  override def main() = foldhood(0)(_ + _) { nbr { 1 } }
}

class CountNeighboursExceptMyself extends AggregateProgram {
  override def main() = foldhood(0)(_ + _) {
    if (nbr { mid() } == mid()) 0 else 1
  }
}

class MaxId extends AggregateProgram {
  override def main() = {
    val maxId = foldhood(Int.MinValue)(Math.max(_, _)) { nbr(mid()) }
    (mid(), maxId)
  }
}

class Gradient extends AggregateProgram {

  def isSource = sense[Boolean](SensorEnum.SENS1.name)
  def isObstacle = sense[Boolean](SensorEnum.SENS2.name)
  def nbrRange = nbrvar[Double](NBR_RANGE_NAME)

  override def main(): Double =
    branch (isObstacle) { Double.MaxValue } {
      rep(Double.MaxValue) {
        distance => mux(isSource) { 0.0 } {
          minHoodPlus { nbr { distance } + nbrRange }
        }
      }
    }
}

trait Blocks { self: AggregateProgram =>

  def G[V: OrderingFoldable](source: Boolean)
                            (field: V)
                            (acc: V => V = (v:V)=>v)
                            (metric: => Double = nbrvar[Double](NBR_RANGE_NAME)): V =
    rep((Double.MaxValue, field)) { dv => mux(source) { (0.0, field) }{
        minHoodPlus {
          val (d, v) = nbr { dv }
          (d + metric, acc(v))
        }
      }
    }._2
}


class GradientHop extends AggregateProgram with Blocks {

  def isSource = sense[Boolean](SensorEnum.SENS1.name)

  def hopGradientByG(src: Boolean): Double = G(src)(0)(_ + 1)(1)

  override def main(): Int = hopGradientByG(isSource).toInt
}

class Channel extends AggregateProgram with Blocks {

  override def main() = channel(isSource, isDest, 0.05)

  def isSource = sense[Boolean](SensorEnum.SENS1.name)
  def isDest = sense[Boolean](SensorEnum.SENS2.name)
  def nbrRange(): Double = nbrvar[Double](NBR_RANGE_NAME)

   def distanceTo(source: Boolean): Double =
     G(source)(0.0)(_ + nbrRange)()

  def broadcast[V: OrderingFoldable](source: Boolean, field: V): V =
    G(source)(field)()()

  def distanceBetween(source: Boolean, target: Boolean): Double =
    broadcast(source, distanceTo(target))

  def channel2(source: Boolean, target: Boolean, width: Double): (String, String, String) =
    (distanceTo(source).formatted("%.2f"), distanceTo(target).formatted("%.2f"), distanceBetween(source, target).formatted("%.2f"))

  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width
}

class Timer extends AggregateProgram {

  override def main() = Duration(timer(Duration(10, TimeUnit.SECONDS)), TimeUnit.MILLISECONDS).toSeconds + "ms" //if(channel(isSource, isDest, 0)) 1 else 0

  def T[V](initial: V, floor: V, decay: V => V)
          (implicit ev: Numeric[V]): V = {
    rep(initial) { v =>
      ev.min(initial, ev.max(floor, decay(v)))
    }
  }

  def T[V](initial: V, decay: V => V)
          (implicit ev: Numeric[V]): V = {
    T(initial, ev.zero, decay)
  }

  def T[V](initial: V)
          (implicit ev: Numeric[V]): V = {
    T(initial, (t: V) => ev.minus(t, ev.one))
  }

  def timer[V](length: V)
              (implicit ev: Numeric[V]) =
    T[V](length)

  def limitedMemory[V, T](value: V, expValue: V, timeout: T)
                         (implicit ev: Numeric[T]) = {
    val t = timer[T](timeout)
    (if (ev.gt(t, ev.zero)) value else expValue, t)
  }

  def timer(dur: Duration): Long = {
    val ct = System.nanoTime() // Current time
    val et = ct + dur.toNanos // Time-to-expire (bootstrap)

    rep((et, dur.toNanos)) { case (expTime, remaining) =>
      if (remaining <= 0) (et, 0)
      else (expTime, expTime - ct)
    }._2 // Selects the component expressing remaining time
  }
}

class SparseChoice extends AggregateProgram {

  override def main() = S(0.35, nbrRange) //if(channel(isSource, isDest, 0)) 1 else 0

  def isSource = sense[Boolean](SensorEnum.SOURCE.name)

  def isDest = sense[Boolean](SensorEnum.OBSTACLE.name)

  def nbrRange(): Double = nbrvar[Double](NBR_RANGE_NAME)

  def G[V: OrderingFoldable](source: Boolean, field: V, acc: V => V, metric: => Double): V =
    rep((Double.MaxValue, field)) { dv =>
      mux(source) {
        (0.0, field)
      } {
        minHoodPlus {
          val (d, v) = nbr {
            (dv._1, dv._2)
          }
          (d + metric, acc(v))
        }
      }
    }._2

  def distanceTo(source: Boolean): Double =
    G[Double](source, 0, _ + nbrRange(), nbrRange())

  def S(grain: Double,
        metric: => Double): Boolean =
    breakUsingUids(randomUid, grain, metric)

  def minId(): ID = {
    rep(Int.MaxValue) { mmid => math.min(mid(), minHood(nbr {
      mmid
    }))
    }
  }

  def S2(grain: Double): Boolean =
    branch(distanceTo(mid() == minId()) < grain) {
      mid() == minId()
    } {
      S2(grain)
    }

  /**
    * Generates a field of random unique identifiers.
    *
    * @return a tuple where the first element is a random number,
    *         end the second element is the device identifier to
    *         ensure uniqueness of the field elements.
    */
  def randomUid: (Double, ID) = rep((Math.random()), mid()) { v =>
    (v._1, mid())
  }

  /**
    * Breaks simmetry using UIDs. UIDs are used to break symmetry
    * by a competition between devices for leadership.
    */
  def breakUsingUids(uid: (Double, ID),
                     grain: Double,
                     metric: => Double): Boolean =
  // Initially, each device is a candidate leader, competing for leadership.
  uid == rep(uid) { lead: (Double, ID) =>
    // Distance from current device (uid) to the current leader (lead).
    val dist = G[Double](uid == lead, 0, (_: Double) + metric, metric)

    // Initially, current device is candidate, so the distance ('dist')
    // will be 0; the same will be for other devices.
    // To solve the conflict, devices abdicate in favor of devices with
    // lowest UID, according to 'distanceCompetition'.
    distanceCompetition(dist, lead, uid, grain, metric)
  }

  /**
    * Candidate leader devices surrender leadership to the lowest nearby UID.
    *
    * @return
    */
  def distanceCompetition(d: Double,
                          lead: (Double, ID),
                          uid: (Double, ID),
                          grain: Double,
                          metric: => Double) = {
    val inf: (Double, ID) = (Double.PositiveInfinity, uid._2)
    mux(d > grain) {
      // If the current device has a distance to the current candidate leader
      //   which is > grain, then the device candidate itself for another region.
      // Remember: 'grain' represents, in the algorithm,
      //   the mean distance between two leaders.
      uid
    } {
      mux(d >= (0.5 * grain)) {
        // If the current device is at an intermediate distance to the
        //   candidate leader, then it abdicates (by returning 'inf').
        inf
      } {
        // Otherwise, elect the leader with lowest UID.
        // Note: it works because Tuple2 has an OrderingFoldable where
        //   the min(t1,t2) is defined according the 1st element, or
        //   according to the 2nd elem in case of breakeven on the first one.
        //   (minHood uses min to select the candidate leader tuple)
        minHood {
          mux(nbr {
            d
          } + metric >= 0.5 * grain) {
            nbr {
              inf
            }
          } {
            nbr {
              lead
            }
          }
        }
      }
    }
  }


}
