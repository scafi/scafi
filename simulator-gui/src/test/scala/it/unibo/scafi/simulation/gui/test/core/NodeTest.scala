package it.unibo.scafi.simulation.gui.test.core

import it.unibo.scafi.simulation.gui.model.common.world.CartesianMetric
import it.unibo.scafi.simulation.gui.model.core._
import it.unibo.scafi.simulation.gui.model.space.Position
import org.scalatest.{FunSpec, Matchers}
class NodeTest extends FunSpec with Matchers {
  val checkThat = new ItWord
  val w : World = new World {
    /**
      * the type of node in this world
      */
    override type NODE = MyNode
    /**
      * the type of boundary of the world
      */
    override type B = Boundary
    /**
      * the type of metric in this world
      */
    override type M = CartesianMetric.type
    /**
      * The metric of this world
      */
    override val metric: Metric = CartesianMetric
    /**
      * A boundary of the world (a world can has no boundary)
      */
    override val bounday: Option[Boundary] = None

    /**
      * get all nodes on this world
      */
    override def nodes: Set[NODE] = ???

    /**
      * return a specific node on the world
      *
      * @param id
      * the id of the node
      * @return
      * the node if it is in the world
      */
    override def apply(id: Int): Option[NODE] = ???
  }
}
class MyNode extends Node {
  /**
    * the type of id
    */
  override type ID = Int
  /**
    * a generic position of the node
    */
  override type P = Position
  /**
    * a shape that describe the node
    */
  override type SHAPE = Shape
  /**
    * the devices associated to the node
    */
  override type DEVICE = MyDevice
  /**
    * the id associated with this node
    */
  override val id: ID = 1

  /**
    * @return
    * the current position
    */
  override def position: Position = Position.ZERO

  /**
    *
    * @return
    * the shape of the node
    */
  override def shape: Shape = ???

  /**
    * @return
    * all devices attach on this node
    */
  override def devices: Set[DEVICE] = ???

  /**
    *
    * @param name of device
    * @return a device if it is attach on the node
    */
  override def getDevice(name: String): Option[DEVICE] = Some(new MyDevice())
}

class MyDevice extends Device {
  /**
    * the name of device
    */
  override type NAME = String
  /**
    * the node where the device is attached
    */
  override type NODE = MyNode
  override val node: NODE = new MyNode()
  override val name: NAME = "Ciao"

  /**
    * enable the device
    */
  override def enable: Unit = ???

  /**
    * disable the device
    */
  override def disable: Unit = ???
}