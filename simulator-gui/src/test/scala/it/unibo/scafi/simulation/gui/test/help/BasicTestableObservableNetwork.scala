package it.unibo.scafi.simulation.gui.test.help
/* TO DELETE
import it.unibo.scafi.simulation.gui.model.common.network.ObservableNetwork
import it.unibo.scafi.simulation.gui.model.common.network.TopologyDefinition.RandomTopology

class BasicTestableObservableNetwork extends BasicTestableObservableWorld with ObservableNetwork {
  private var _neighbours : Map[NODE,Set[NODE]] = Map[NODE,Set[NODE]]()
  override protected def addStrategy(node: NODE, nodes: Set[NODE]): Unit =_neighbours += node -> nodes

  override protected def removeStrategy(node: NODE): Unit = _neighbours -= node

  override type T = RandomTopology[NODE]
  override val topology: T = new RandomTopology[NODE]

  /**
    * the neighbour in the world
    *
    * @return
    */
  override def neighbours(): Map[NODE, Set[NODE]] = this._neighbours
}
*/