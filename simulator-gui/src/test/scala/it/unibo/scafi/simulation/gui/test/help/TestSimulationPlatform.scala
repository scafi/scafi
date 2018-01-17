package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.common.network.TopologyDefinition.RandomTopology
import it.unibo.scafi.simulation.gui.model.common.world.MetricDefinition.CartesianMetric
import it.unibo.scafi.simulation.gui.model.core.Boundary
import it.unibo.scafi.simulation.gui.model.simulation.SimulationPlatform
import it.unibo.scafi.simulation.gui.pattern.observer.SimpleSource

class TestSimulationPlatform extends SimulationPlatform with SimpleSource {
  override type NODE = BasicTestableAggregateNode
  override type O = BasicTestableWorldObserver[NODE]
  override type T = RandomTopology[NODE]
  override val topology: T = new RandomTopology[NODE]
  override type B = Boundary[NODE]
  override type M = CartesianMetric[NODE#P]
  override val metric: M = new CartesianMetric[NODE#P]
  override val boundary: Option[B] = None
}
