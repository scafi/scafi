package it.unibo.scafi.simulation.gui.model.simulation

import it.unibo.scafi.simulation.gui.model.aggregate.implementation.mutable.{AbstractAggregateWorld, AggregateNodeDefinition}
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld
import it.unibo.scafi.simulation.gui.model.core.{Shape, World}
import it.unibo.scafi.simulation.gui.model.sensor.SensorNetwork
import it.unibo.scafi.simulation.gui.model.sensor.implementation.mutable.{SensorDefinition, SensorWorld}
import it.unibo.scafi.simulation.gui.model.space.{Point2D, Point3D}
import it.unibo.scafi.simulation.gui.pattern.observer.{PrioritySource, SimpleSource}

import scala.collection.mutable

object PlatformDefinition {

  /**
    * a generic platform
    */
  trait GenericPlatform extends AbstractAggregateWorld with SensorNetwork with ConnectedWorld with SimpleSource

  /**
    * a sensor platform :
    * describe a connected world with a sensor network
    */
  trait SensorPlatform extends SensorWorld with ConnectedWorld with SensorDefinition with AggregateNodeDefinition with SimpleSource

  /**
    * describe a 3D world
    */
  trait World3D {
    self : World =>
    override type P = Point3D
    override type S = Shape
  }

  /**
    * describe a 2D world
    */
  trait World2D {
    self : World =>
    override type P = Point2D
    override type S = Shape
  }

  //TODO QUA METTI LA MUTABILITA O L'IMMUTABILITA'
  trait StandardNodeDefinition {
    self: SensorPlatform =>

    override type NODE = Node

    override type MUTABLE_NODE = AbstractMutableNode

    override type NODE_PRODUCER = AbstractNodeBuilder

    private class StandardNode(id: ID, position: P, shape: Option[S]) extends AbstractMutableNode(id, position, shape) {
      override def view: NODE = this
    }

    class NodeBuilder(id : ID, position : P, shape : Option[S] = None, producer : List[DEVICE_PRODUCER] = List.empty)
      extends AbstractNodeBuilder(id,shape,position,producer) {

      override def build(): AbstractMutableNode = {
        val node = new StandardNode(id,position,shape)
        producer map {_.build} foreach {node.addDevice(_)}
        node
      }
    }
  }

  trait StandardNetwork  {
    self: SensorPlatform =>

    override type NET = Network

    val network : NET = new NetworkImpl

    private class NetworkImpl extends Network {
      private var neigh : mutable.Map[ID,Set[ID]] = mutable.Map.empty
      /**
        * the neighbours of a node
        *
        * @param n the node
        * @return a set of neighbours
        */
      override def neighbours(n: ID): Set[ID] = neigh.getOrElse(n,Set())

      /**
        * the neighbour in the world
        *
        * @return the network
        */
      override def neighbours(): Map[ID, Set[ID]] = neigh toMap

      /**
        * set a neighbours of a node
        *
        * @param node      the node in thw world
        * @param neighbour the neighbour
        */
      override def setNeighbours(node: ID, neighbour: Set[ID]): Unit = neigh += node -> neighbour
    }

  }
}
