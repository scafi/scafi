/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BuildingBlocks {
  self: StandardLibrary.Subcomponent =>

  trait BuildingBlocksInterface extends
         SimpleGradientsInterface
    with BlockGInterface
    with BlockCInterface
    with BlockSInterface
    with TimeUtilsInterface
    with BlocksWithGCInterface
    with StateManagement {
    self: ScafiBaseLanguage with StandardSensors with LanguageDependant with NeighbourhoodSensorReader =>
  }
  
  trait BuildingBlocks_ScafiStandard extends BuildingBlocksInterface
    with FieldUtils
    with SimpleGradients_ScafiStandard
    with BlockG_ScafiStandard
    with BlockC_ScafiStandard
    with BlockS_ScafiStandard
    with TimeUtils_ScafiStandard
    with BlocksWithGC_ScafiStandard
    with StateManagement
    with LanguageDependant_ScafiStandard {
    self: ScafiStandardLanguage with StandardSensors =>
  }

  trait BuildingBlocks_ScafiFC extends BuildingBlocksInterface
    with SimpleGradients_ScafiFC
    with BlockG_ScafiFC
    with BlockC_ScafiFC
    with BlockS_ScafiFC
    with TimeUtils_ScafiFC
    with BlocksWithGC_ScafiFC
    with StateManagement
    with LanguageDependant_ScafiFC {
    self: ScafiFCLanguage with StandardSensors =>
  }
}
