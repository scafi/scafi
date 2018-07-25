package it.unibo.scafi.simulation.gui.model.common

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.space.Point2D

/**
  * some standard metric definition
  */
trait MetricDefinition {
  self : World =>

  implicit def funToMetric(f : (P) => Boolean) : Metric = new Metric {
    override def positionAllowed(p: P): Boolean = f(p)
  }
  /**
    * allow all position
    */
  val cartesinMetric : Metric = (p : P) => true
}
