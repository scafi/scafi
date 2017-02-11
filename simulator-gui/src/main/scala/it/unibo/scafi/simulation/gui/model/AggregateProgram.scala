package it.unibo.scafi.simulation.gui.model

import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation._

/**
  * @author Roberto Casadei
  *
  */

trait AggregateProgram extends AggregateProgramSpecification with ExecutionTemplate with Constructs with Builtins {
  override type MainResult = Any
}



