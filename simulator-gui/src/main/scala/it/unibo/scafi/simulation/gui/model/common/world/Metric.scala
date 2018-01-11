package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.Metric
import it.unibo.scafi.simulation.gui.model.space.Position

object CartesianMetric extends Metric {
  override def positionAllowed(p: Position): Boolean = true
}