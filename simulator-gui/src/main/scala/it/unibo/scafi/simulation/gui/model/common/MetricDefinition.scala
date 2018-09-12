package it.unibo.scafi.simulation.gui.model.common

import it.unibo.scafi.simulation.gui.model.core.World

/**
  * some standard metric definition
  * use to world to verify the correctness
  * of node position
  */
trait MetricDefinition {
  self : World =>

  implicit def funToMetric(f : (P) => Boolean) : Metric = new Metric {
    override def positionAllowed(p: P): Boolean = f(p)
  }
  /**
    * allow all position like a cartasian space
    */
  val cartesianMetric : Metric = (_ : P) => true
}
