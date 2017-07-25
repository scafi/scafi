package it.unibo.scafi.lib

import it.unibo.scafi.core.Core
import it.unibo.scafi.incarnations.Incarnation

/**
  * @author Roberto Casadei
  *
  */

trait StandardLibrary extends
         Stdlib_BlockG
    with Stdlib_BlockC
    with Stdlib_BlockT
    with Stdlib_BlockT2
    with Stdlib_BlockS
    with Stdlib_BlocksWithGC
    with Stdlib_BuildingBlocks
    with FieldUtils { self: Incarnation => }

object StandardLibrary {
  type Subcomponent = StandardLibrary with Incarnation
}