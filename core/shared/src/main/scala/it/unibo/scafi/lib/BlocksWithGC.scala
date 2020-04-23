/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BlocksWithGC {
  self: StandardLibrary.Subcomponent =>

  trait BlocksWithGC extends BlockG with BlockC {
    self: FieldCalculusSyntax with StandardSensors =>

    def summarize(sink: Boolean, acc: (Double, Double) => Double, local: Double, Null: Double): Double =
      broadcast(sink, C(distanceTo(sink), acc, local, Null))

    def average(sink: Boolean, value: Double): Double =
      summarize(sink, (a, b) => {
        a + b
      }, value, 0.0) / summarize(sink, (a, b) => a + b, 1, 0.0)
  }
}
