package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.incarnations.BasicAbstractSpatialSimulationIncarnation
import it.unibo.scafi.space.Point3D

object ScafiWorldIncarnation extends BasicAbstractSpatialSimulationIncarnation {
  override type P = Point3D
}
