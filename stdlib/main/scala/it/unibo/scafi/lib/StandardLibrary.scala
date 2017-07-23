package it.unibo.scafi.lib

import it.unibo.scafi.incarnations.Incarnation

/**
  * @author Roberto Casadei
  *
  */

trait StandardLibrary
  with BuildingBlocks { self: Incarnation => }

object StandardLibrary {
  type Subcomponent = StandardLibrary with Incarnation
}