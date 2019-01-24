package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.incarnations.BasicAbstractSpatialSimulationIncarnation
import it.unibo.scafi.space.Point3D


/**
  * describe an incarnation of a scafi platform
  */
object ScafiWorldIncarnation extends BasicAbstractSpatialSimulationIncarnation {
  override type P = Point3D
}