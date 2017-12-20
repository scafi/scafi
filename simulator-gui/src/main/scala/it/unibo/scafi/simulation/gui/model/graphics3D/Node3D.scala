package it.unibo.scafi.simulation.gui.model.graphics3D

import it.unibo.scafi.simulation.gui.model.core.{GraphicsNode, Shape}
import it.unibo.scafi.simulation.gui.model.space.Position3D
trait Node3D extends GraphicsNode {
  override type SHAPE = Shape3D
  override type ID = Int

  override type P = Position3D

  trait Shape3D extends Shape {
    //TODO
  }
}
