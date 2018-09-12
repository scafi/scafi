package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.common.world.AbstractObservableWorld
import it.unibo.scafi.simulation.gui.model.common.{BoundaryDefinition, MetricDefinition}
import it.unibo.scafi.simulation.gui.model.core.Shape
import it.unibo.scafi.simulation.gui.model.space.Point3D

class StandardWorldDefinition extends MetricDefinition with BoundaryDefinition {
  self : AbstractObservableWorld =>
  override type ID = Int
  override type NAME = String
  override type P = Point3D
  override type S = Shape
  override type NODE = Node
  override type B = Boundary
  override type M = Metric

  var metric: M = this.cartesianMetric

  var boundary: Option[B] = None
}
