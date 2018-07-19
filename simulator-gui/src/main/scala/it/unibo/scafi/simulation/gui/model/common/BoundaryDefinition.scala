package it.unibo.scafi.simulation.gui.model.common

import it.unibo.scafi.simulation.gui.model.core.{Shape, World}
import it.unibo.scafi.simulation.gui.model.space.{Point2D, Point3D}

/**
  * definition of boundary
  */
trait BoundaryDefinition {
  self : World =>

  type BOUND = (P, Shape)

  /**
    * a boundary with shape bound
    * @param inclusiveBound the node inside the inclusive bound are accepted
    * @param exclusiveBounds the node inside the exclusive bound are rejected
    */
  class ShapeBoundary(val inclusiveBound : Shape, exclusiveBounds : BOUND *) extends Boundary {
    override def nodeAllowed(p: P, s: Option[S]): Boolean = {
      //if the node it is outside the inclusive bound the node is reject
      if(!inclusiveBound.contains(p)) return false

      //check for all bound if the node is inside or outside
      exclusiveBounds forall ( bound => {
        p match {
          case Point3D(x,y,z) => {
            val boundPosition : Point3D = bound._1.asInstanceOf[Point3D]
            !bound._2.contains(Point3D(x - boundPosition.x, y - boundPosition.y, z - boundPosition.z))
          }
          case Point2D(x,y) => {
            val boundPosition : Point2D = bound._1.asInstanceOf[Point2D]
            !bound._2.contains(Point2D(x - boundPosition.x, y - boundPosition.y))
          }
          case _ => false
        }
      })
    }
  }
}
