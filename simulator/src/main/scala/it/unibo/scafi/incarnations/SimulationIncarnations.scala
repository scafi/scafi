package it.unibo.scafi.incarnations

import it.unibo.scafi.simulation.{Simulation, SpatialSimulation}
import it.unibo.scafi.space.BasicSpatialAbstraction

/**
  * @author Roberto Casadei
  *
  */

object BasicSimulationIncarnation
  extends BasicAbstractIncarnation
    with BasicPrograms
    with Simulation {
}

class BasicAbstractSpatialSimulationIncarnation
  extends BasicAbstractIncarnation
    with SpatialSimulation
    with BasicSpatialAbstraction