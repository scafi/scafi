package it.unibo.scafi.simulation.gui.model.common

import it.unibo.scafi.simulation.gui.model.core.World

trait MetricDefinition {
  self : World =>

  val cartesinMetric = new Metric {
    override def positionAllowed(p: P): Boolean = true
  }
}
