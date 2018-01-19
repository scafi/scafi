package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.Boundary
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.space.Point2D

object BoundaryDefinition {

  /**
    * @param width
    * @param height
    */
  class RectangleBoundary(width : Double, height : Double) extends Boundary[Point2D,Shape2D] {
    override def nodeAllowed(p: Point2D, s: Option[Shape2D]): Boolean = (p.x > -width /2 && p.x < width / 2) && (p.y > -height/2 && p.y < height/2)
  }
}
