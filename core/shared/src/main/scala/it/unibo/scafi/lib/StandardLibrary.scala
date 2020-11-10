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
    with StdLib_DynamicCode
    with StdLib_LanguageDependant { self: Incarnation =>

  trait Libraries {
    type BlockG <: BlockGInterface
    type SimpleGradients <: SimpleGradientsInterface
    type BlockC <: BlockCInterface
    type BlocksWithGC <: BlocksWithGCInterface
    type BuildingBlocks <: BuildingBlocksInterface
    type BlockS <: BlockSInterface
    type TimeUtils <: TimeUtilsInterface
    type BlockT <: BlockTInterface
  }

  object ScafiStandardLibraries extends Libraries {
    override type BlockG = BlockG_ScafiStandard
    override type SimpleGradients = SimpleGradients_ScafiStandard
    type Gradients = Gradients_ScafiStandard
    override type BlockC = BlockC_ScafiStandard
    override type BlocksWithGC = BlocksWithGC_ScafiStandard
    override type BuildingBlocks = BuildingBlocks_ScafiStandard
    override type BlockS = BlockS_ScafiStandard
    override type TimeUtils = TimeUtils_ScafiStandard
    override type BlockT = BlockT_ScafiStandard
  }

  object ScafiFCLibraries extends Libraries {
    override type BlockG = BlockG_ScafiFC
    override type SimpleGradients = SimpleGradients_ScafiFC
    override type BlocksWithGC = BlocksWithGC_ScafiFC
    override type BuildingBlocks = BuildingBlocks_ScafiFC
    override type BlockC = BlockC_ScafiFC
    override type BlockS = BlockS_ScafiFC
    override type TimeUtils = TimeUtils_ScafiFC
    override type BlockT = BlockT_ScafiFC
  }
}

object StandardLibrary {
  type Subcomponent = StandardLibrary with Incarnation
}
