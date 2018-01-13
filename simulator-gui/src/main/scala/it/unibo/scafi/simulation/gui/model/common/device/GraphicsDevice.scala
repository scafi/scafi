package it.unibo.scafi.simulation.gui.model.common.device

import it.unibo.scafi.simulation.gui.model.core.{Device, Shape}

trait GraphicsDevice {
  self : Device =>

  type SHAPE <: Shape
}
