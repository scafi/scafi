package it.unibo.scafi.simulation.s2.frontend.model.common.world

import it.unibo.scafi.simulation.s2.frontend.model.common.world.CommonWorldEvent.NodesAdded
import it.unibo.scafi.simulation.s2.frontend.model.common.world.CommonWorldEvent.NodesRemoved
/**
 * a world mutable, the observer want to see the changes in the world
 */
trait AbstractObservableWorld extends ObservableWorld {
  self: ObservableWorld.Dependency =>

  private var internalMap: Map[ID, self.MUTABLE_NODE] = Map[ID, MUTABLE_NODE]()

  override def clear(): Unit = internalMap = internalMap.empty

  override def apply(id: ID): Option[NODE] = {
    val node = internalMap.get(id)
    if (node.isDefined) Some(node.get.view) else None
  }

  override def apply(ids: Set[ID]): Set[NODE] = internalMap.filter(x => ids.contains(x._1)).map(_._2.view).toSet

  override def nodes: Set[NODE] = internalMap.values.map(_.view).toSet

  override def insertNode(producer: NODE_PRODUCER): Boolean = {
    // create a new node by producer passed
    val node = producer.build()
    // look if the node is legit
    if (internalMap.contains(node.id)) {
      false
    } else {
      // add node into node map
      internalMap += node.id -> node
      // notify the observers of world changes
      notify(NodeEvent(node.id, NodesAdded))
      true
    }

  }

  override def removeNode(id: ID): Boolean = {
    // take the node by his id
    val node = internalMap.get(id)
    // id node is empty it mean that the node isn't in the world
    if (node.isEmpty) {
      false
    } else {
      // if the node is in the world i remove it
      internalMap -= node.get.id
      // notify the observer of world changes
      notify(NodeEvent(node.get.id, NodesRemoved))
      true
    }
  }

  /**
   * try to find node with selected id, if the id isn't in the world throw IllegalArgumentException
   * @param id
   *   the node id
   * @return
   *   the node associated with the id
   */
  protected def getNodeOrThrows(id: ID): MUTABLE_NODE = internalMap(id)
}
