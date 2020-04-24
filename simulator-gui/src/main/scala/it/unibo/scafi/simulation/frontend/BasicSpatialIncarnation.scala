/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

import it.unibo.scafi.incarnations.BasicAbstractSpatialSimulationIncarnation
import it.unibo.scafi.space.Point3D

object BasicSpatialIncarnation extends BasicAbstractSpatialSimulationIncarnation {
  override type P = Point3D

  trait MyEuclideanStrategy extends EuclideanStrategy {
    this: Basic3DSpace[_] =>
    override val proximityThreshold = 0.15
  }

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) with MyEuclideanStrategy
}
