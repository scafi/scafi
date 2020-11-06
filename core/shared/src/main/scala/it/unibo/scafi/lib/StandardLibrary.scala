/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

import it.unibo.scafi.incarnations.Incarnation

trait StandardLibrary extends
         StdLib_BlockG
    with StdLib_Gradients
    with StdLib_BlockC
    with StdLib_BlockS
    with StdLib_BlocksWithGC
    with StdLib_BuildingBlocks
    with StdLib_FieldUtils
    with StdLib_TimeUtils
    with StdLib_StateManagement
    with StdLib_GenericUtils
    with StdLib_Processes
    with StdLib_NewProcesses
    with StdLib_DynamicCode { self: Incarnation =>

  trait ScafiStandardLanguageLibraries extends
         BlockC_ScafiStandard
    with BlocksWithGC_ScafiStandard
    with BuildingBlocks_ScafiStandard {
    self: ScafiStandardLanguage =>
  }

  trait ScafiFCLanguageLibraries extends
         BlockC_ScafiFC
    with BlocksWithGC_ScafiFC
    with BuildingBlocks_ScafiFC {
    self: ScafiFCLanguage =>
  }
}

object StandardLibrary {
  type Subcomponent = StandardLibrary with Incarnation
}
