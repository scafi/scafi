/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BuildingBlocks {
  self: StandardLibrary.Subcomponent =>

  trait BuildingBlocks extends Gradients with FieldUtils
    with BlockG with BlockC with BlockS with BlockT with TimeUtils with BlocksWithGC with StateManagement {
    self: ScafiStandardLanguage with StandardSensors with BuildingBlocks_LanguageDependant =>
  }

  private[lib] trait BuildingBlocks_LanguageDependant extends
         BlockC_LanguageDependant
    with BlocksWithGC_LanguageDependant

  private[lib] trait BuildingBlocks_ScafiStandard extends BuildingBlocks_LanguageDependant
    with BlockC_ScafiStandard
    with BlocksWithGC_ScafiStandard {
    self: ScafiStandardLanguage =>
  }

  private[lib] trait BuildingBlocks_ScafiFC extends BuildingBlocks_LanguageDependant
    with BlockC_ScafiFC
    with BlocksWithGC_ScafiFC {
    self: ScafiFCLanguage =>
  }
}
