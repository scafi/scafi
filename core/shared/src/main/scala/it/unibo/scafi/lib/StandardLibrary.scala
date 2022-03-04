/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

import it.unibo.scafi.incarnations.Incarnation

trait StandardLibrary extends
         StdLibBlockG
    with StdLibGradients
    with StdLibBlockC
    with StdLibBlockS
    with StdLibBlocksWithGC
    with StdLibBuildingBlocks
    with StdLibExplicitFields
    with StdLibFieldUtils
    with StdLibTimeUtils
    with StdLibStateManagement
    with StdLibGenericUtils
    with StdLibProcesses
    with StdLibDynamicCode { self: Incarnation => }

object StandardLibrary {
  type Subcomponent = StandardLibrary with Incarnation
}
