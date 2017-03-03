package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import Builtins._

/**
  * @author Roberto Casadei
  *
  */
trait BlockG { self: AggregateProgram with SensorDefinitions =>

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

  def G2[V: OrderingFoldable](source: Boolean)(field: V)(acc: V => V)(metric: => Double = nbrRange): V =
    rep((Double.MaxValue, field)) { case (d,v) =>
      mux(source) { (0.0, field) } {
        minHoodPlus { (nbr{d} + metric, acc(nbr{v})) }
      }
    }._2

  def distanceTo(source: Boolean): Double =
    G2(source)(0.0)(_ + nbrRange)()

  def broadcast[V: OrderingFoldable](source: Boolean, field: V): V =
    G2(source)(field)(v=>v)()

  def distanceBetween(source: Boolean, target: Boolean): Double =
    broadcast(source, distanceTo(target))
}
