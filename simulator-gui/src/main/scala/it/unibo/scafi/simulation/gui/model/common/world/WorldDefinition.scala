package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.{Shape, World}
import it.unibo.scafi.simulation.gui.model.space.{Point2D, Point3D}

/**
  * a standard world definition
  * at this moment there isn't
  * like between position type (point3d,point2d)
  * and shape type
  */
object WorldDefinition {
  /**
    * describe a 3D world
    */
  trait World3D {
    self : World =>
    override type P = Point3D
    override type S = Shape
  }

  /**
    * describe a 2D world
    */
  trait World2D {
    self : World =>
    override type P = Point2D
    override type S = Shape
  }
}
