package it.unibo.scafi.incarnations

import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.simulation.{Simulation, SpatialSimulation}
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point3D}
import it.unibo.scafi.time.BasicTimeAbstraction

/**
  * @author Roberto Casadei
  *
  */

trait BasicAbstractSimulationIncarnation
  extends BasicAbstractIncarnation
  with Simulation
  with BasicTimeAbstraction {

  override val LSNS_RANDOM: String = "randomGenerator"
}

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