package it.unibo.scafi.simulation.gui.model.graphics3D

import it.unibo.scafi.simulation.gui.model.core.GraphicsNode
import it.unibo.scafi.simulation.gui.model.space.Position3D
trait Node3D extends GraphicsNode {

  override type ID = Int

  override type P = Position3D
}
