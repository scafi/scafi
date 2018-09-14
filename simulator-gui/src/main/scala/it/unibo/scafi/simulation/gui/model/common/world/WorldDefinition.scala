package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.space.Shape

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
    override type S = Shape
  }
}
