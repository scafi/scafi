package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.common.BoundaryDefinition
import it.unibo.scafi.simulation.gui.model.common.world.AbstractObservableWorld
import it.unibo.scafi.space.Shape
import it.unibo.scafi.space.SpatialAbstraction.Bound

class StandardWorldDefinition extends BoundaryDefinition {
  self : AbstractObservableWorld =>
  override type ID = Int
  override type NAME = String
  override type S = Shape
  override type NODE = Node

  var boundary: Option[Bound] = None
}
