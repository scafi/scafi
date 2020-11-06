/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BlocksWithGC {
  self: StandardLibrary.Subcomponent =>

  trait BlocksWithGC extends BlockG with BlockC {
    self: ScafiStandardLanguage with StandardSensors with BlocksWithGC_LanguageDependant =>

    def summarize(sink: Boolean, acc: (Double, Double) => Double, local: Double, Null: Double): Double =
      broadcast(sink, C(distanceTo(sink), acc, local, Null))

    def average(sink: Boolean, value: Double): Double =
      summarize(sink, _ + _, value, 0.0) / summarize(sink, _ + _, 1, 0.0)
  }

  private[lib] trait BlocksWithGC_LanguageDependant extends BlockC_LanguageDependant

  private[lib] trait BlocksWithGC_ScafiStandard extends BlocksWithGC_LanguageDependant
    with BlockC_ScafiStandard {
    self: ScafiStandardLanguage =>
  }

  private[lib] trait BlocksWithGC_ScafiFC extends BlocksWithGC_LanguageDependant
    with BlockC_ScafiFC {
    self: ScafiFCLanguage =>
  }
}
