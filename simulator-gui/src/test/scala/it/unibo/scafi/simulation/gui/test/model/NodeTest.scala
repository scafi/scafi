package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.aggregate.{AggregateDevice, AggregateNode}
import it.unibo.scafi.simulation.gui.model.common.device.GraphicsDevice
import it.unibo.scafi.simulation.gui.model.common.sensor.Sensor
import it.unibo.scafi.simulation.gui.model.graphics2D.{BasicShape2D, Shape2D}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D}
import org.scalatest.{FunSpec, Matchers}

class NodeTest extends FunSpec with Matchers {
  val checkThat = new ItWord


  val node = new RootNode[RootDevice](id = 1,position = Point.ZERO)
  val anotherNode = new RootNode[RootDevice](id = 2,position = Point.ZERO)
  val simpleDevice : RootDevice = new RootDevice("Pinco",false)

  val sensor : RootDevice= new RootDevice("Pillo",false) with Sensor {

    override type VALUE = String

    override def value: String = "I'm sensors.."
  }

  val graphicsSensor : RootDevice = new RootDevice("Pillo",false) with Sensor with GraphicsDevice {
    /**
      * the value of the sensor
      */
    override type VALUE = SHAPE

    /**
      * get the current value of the device
      *
      * @return the value
      */
    override def value: VALUE = BasicShape2D.Rectangle(0,0,1,1,0)

    override type SHAPE = Shape2D
  }
  checkThat("do immutability, if a add twice time device in the same node, no exception throw") {
    try {
      node.addDevice(simpleDevice)
      node.addDevice(simpleDevice)
    } catch {
      case _ => fail()
    }
  }
  checkThat("if some device has parent, exception throw"){
    val nodeWithDevice = node.addDevice(simpleDevice).get
    val deviceAdded : RootDevice = nodeWithDevice.getDevice(simpleDevice.name).get
    try {
      anotherNode.addDevice(deviceAdded)
    } catch {
      case e : IllegalArgumentException =>
      case _ => fail()
    }
  }

}

class RootDevice(override val name : String, override val state : Boolean, node : Option[AggregateNode] = None) extends AggregateDevice {
  /**
    * the name of device
    */
  override type NAME = String
  /**
    * the node where the device is attached
    */
  override type NODE = AggregateNode

  override protected def createDevice(state: Boolean, parent: Option[AggregateNode]): this.type = new RootDevice(name,state,parent).asInstanceOf[this.type]
}

class RootNode[D <: RootDevice](override val id : Int, override val position : Point2D, devs : Set[D] = Set[D]()) extends AggregateNode {
  override type DEVICE = D

  require(devs forall {_.node.isEmpty})
  private var _devices : Set[DEVICE] = devs.asInstanceOf[Set[DEVICE]]

  override protected def copyNode(p: P, shape: Option[SHAPE], d: Set[DEVICE]): this.type = new RootNode(id,position,d).asInstanceOf[this.type]

  /**
    * the type of id
    */
  override type ID = Int
  /**
    * a generic position of the node
    */
  override type P = Point2D
  /**
    * a shape that describe the node
    */
  override type SHAPE = Shape2D

  override def shape: Option[SHAPE] = None

  override def devices: Set[DEVICE] = this._devices

  override def getDevice(name: DEVICE#NAME): Option[DEVICE] = this._devices.find(_.name == name)
}
