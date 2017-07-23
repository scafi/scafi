package it.unibo.scafi.lib

/**
  * @author Roberto Casadei
  *
  */

trait BlockG {
  self: StandardLibrary.Subcomponent =>

  import Builtins._

  trait BlockG {
    self: AggregateProgram with StandardSensors =>

    def G[V: Bounded](source: Boolean, field: V, acc: V => V, metric: => Double): V =
      rep((Double.MaxValue, field)) { case (dist, value) =>
        mux(source) {
          (0.0, field)
        } {
          minHoodPlus {
            (nbr {
              dist
            } + metric, acc(nbr {
              value
            }))
          }
        }
      }._2

    def G2[V: Bounded](source: Boolean)(field: V)(acc: V => V)(metric: => Double = nbrRange): V =
      G(source, field, acc, metric)

    def distanceTo(source: Boolean): Double =
      G2(source)(0.0)(_ + nbrRange)()

    def broadcast[V: Bounded](source: Boolean, field: V): V =
      G2(source)(field)(v => v)()

    def distanceBetween(source: Boolean, target: Boolean): Double =
      broadcast(source, distanceTo(target))
  }

}