package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.Metric
import it.unibo.scafi.simulation.gui.model.space.{Point}

/**
  * basic metric definition
  */
object MetricDefinition {

  /**
    * allow all position
    */
  object CartesianMetric extends Metric {
    override def positionAllowed(p: Point): Boolean = true
  }
}