package it.unibo.scafi.lib

/**
  * @author Roberto Casadei
  *
  */

trait BlocksWithGC {
  self: StandardLibrary.Subcomponent =>

  import Builtins._

  trait BlocksWithGC {
    self: BlockC with BlockG =>

    def summarize(sink: Boolean, acc: (Double, Double) => Double, local: Double, Null: Double): Double =
      broadcast(sink, C(distanceTo(sink), acc, local, Null))

    def average(sink: Boolean, value: Double): Double =
      summarize(sink, (a, b) => {
        a + b
      }, value, 0.0) / summarize(sink, (a, b) => a + b, 1, 0.0)
  }
}