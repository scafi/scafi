package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorldIncarnation._
/** TODO REPLACE CURRENT SCAFI SIMULATION OBSERVER WITH THIS
trait AbstractScafiSimulation[W <: AggregateWorld] extends ExternalSimulation[W]{
  override type EXTERNAL_SIMULATION <: SpaceAwareSimulator
  override type SIMULATION_PROTOTYPE = BasicScafiPrototype

  trait BasicScafiPrototype {
    def create(internalWorld : W) : EXTERNAL_SIMULATION
  }
}
**/