package it.unibo.scafi.simulation.gui.model.common.device

import it.unibo.scafi.simulation.gui.model.core.{Shape, World}

/**
  * define a device with a graphics component
  */
trait GraphicsDevice {
  self : World#Device =>

  type SHAPE <: Shape
}
