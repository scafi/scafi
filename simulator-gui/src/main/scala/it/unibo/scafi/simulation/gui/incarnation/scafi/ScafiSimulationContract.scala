package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.platform.SpaceAwarePlatform
import it.unibo.scafi.simulation.gui.controller.SimulationContract
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

class ScafiSimulationContract[S <: SpaceAwarePlatform, W <: AggregateWorld, P <: ScafiPrototype] extends SimulationContract[S,W,P] {
  private var currentSimulation : Option[S] = None
  override def getSimulation: Option[S] = this.currentSimulation

  override def initialize(world: W, prototype: P): Unit = {
    require(currentSimulation.isEmpty)
  }

  /**
    * restart the external simulation
    *
    * @param world the internal representation of the world
    */
  override def restart(world: W, prototype: P): Unit = {
    require(currentSimulation.isDefined)
  }
}

trait ScafiPrototype {
  def randomSeed : Long
  def randomDeviceSeed : Long
}
