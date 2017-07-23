package it.unibo.scafi.incarnations

import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.simulation.{Simulation, SpatialSimulation}
import it.unibo.scafi.space.BasicSpatialAbstraction

/**
  * @author Roberto Casadei
  *
  */

object BasicSimulationIncarnation
  extends BasicAbstractIncarnation
    with StandardLibrary
    with BasicPrograms
    with Simulation {
  import Builtins.Bounded
  override implicit val idBounded: Bounded[ID] = Builtins.Bounded.of_i
}

class BasicAbstractSpatialSimulationIncarnation
  extends BasicAbstractIncarnation
    with SpatialSimulation
    with BasicSpatialAbstraction