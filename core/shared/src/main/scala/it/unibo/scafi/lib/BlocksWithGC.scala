/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BlocksWithGC {
  self: StandardLibrary.Subcomponent =>

  trait BlocksWithGCInterface extends BlockGInterface with BlockCInterface {
    self: ScafiBaseLanguage with StandardSensors with LanguageDependant with NeighbourhoodSensorReader =>

    def summarize(sink: Boolean, acc: (Double, Double) => Double, local: Double, Null: Double): Double =
      broadcast(sink, C(distanceTo(sink), acc, local, Null))

    def average(sink: Boolean, value: Double): Double =
      summarize(sink, _ + _, value, 0.0) / summarize(sink, _ + _, 1, 0.0)
  }

  private[lib] trait BlocksWithGC_ScafiStandard extends BlocksWithGCInterface
    with BlockG_ScafiStandard with BlockC_ScafiStandard with LanguageDependant_ScafiStandard {
    self: ScafiStandardLanguage with StandardSensors =>
  }

  private[lib] trait BlocksWithGC_ScafiFC extends BlocksWithGCInterface
    with BlockG_ScafiFC with BlockC_ScafiFC with LanguageDependant_ScafiFC {
    self: ScafiFCLanguage with StandardSensors =>
  }
}
