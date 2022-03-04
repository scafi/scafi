/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.incarnations

import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.simulation.{Simulation, SpatialSimulation}
import it.unibo.scafi.space.BasicSpatialAbstraction
import it.unibo.scafi.time.BasicTimeAbstraction

trait BasicAbstractSimulationIncarnation
  extends BasicAbstractIncarnation
  with Simulation
  with BasicTimeAbstraction

object BasicSimulationIncarnation
  extends BasicAbstractSimulationIncarnation
    with StandardLibrary {

  import Builtins.Bounded
  override implicit val idBounded: Bounded[ID] = Builtins.Bounded.of_i
}

class BasicAbstractSpatialSimulationIncarnation
  extends BasicAbstractSimulationIncarnation
    with SpatialSimulation
    with BasicSpatialAbstraction
