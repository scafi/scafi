package it.unibo.scafi.simulation.gui.launcher.scalaFX

import it.unibo.scafi.simulation.gui.incarnation.scafi.SimpleScafiWorld
import it.unibo.scafi.simulation.gui.model.space.Point3D

import scala.util.Random
//TODO COMPLETE
object WorldConfig {
  import it.unibo.scafi.simulation.gui.incarnation.scafi.SimpleScafiWorld._

  val world = SimpleScafiWorld
  /**
    * defaul prototype
    */
  val defaultNodePrototype = new ExternalNodePrototype(None)
  /**
    * the prototype of node in the world
    */
  var nodeProto : NODE_PROTOTYPE = defaultNodePrototype
  /**
    * the devices attach on a node
    */
  var devs = Set[DEVICE]()

  /**
    * function used to create a device
    * @param n the name
    * @param value the value
    * @tparam V the type of value
    * @return the devices created
    */
  def dev[V](n : Name, value : V = true) : DEVICE = deviceFactory.create(n.name,new ExternalDevicePrototype(value))
  private val deviceProto : DevicePrototype = new ExternalDevicePrototype(true)

  /**
    * used to create a prototype
    * @param shape the shape of node
    * @return the prototype of node
    */
  def NodePrototype(shape : S) : NODE_PROTOTYPE = new ExternalNodePrototype(Some(shape))

  trait Name {
    val name : String
  }

  /**
    * all sensor name accept
    */
  val source : Name = new Name{val name = "source"}
  val destination : Name = new Name{val name = "destination"}
  val obstacle : Name = new Name{val name = "obstacle"}
  val id : Name = new Name {val name = "id"}
  val gsensor : Name = new Name{val name = "value"}
  val gsensor1 : Name = new Name{val name = "output"}
  val gsesonr2 : Name = new Name{val name = "generic2"}


  /**
    * initialize a world 2D in a randomize way
    * @param number the number of element
    */
  def randomize2D(number : Int,maxPoint : Int): Unit = {
    val r = new Random()
    //all nodes on the same 2d planes
    val z = 0
    val node : Set[NODE] = ((0 to number) map {
      nodeFactory.create(_,Point3D(r.nextInt(maxPoint),r.nextInt(maxPoint),z),devs,nodeProto)
    } toSet)
    world clear()
    world ++ node
  }

  def gridLike2D(row : Int, column : Int, distance : Double): Unit = {
    var node : Set[NODE] = Set()
    val z = 0
    var nodes = 0;
    for(i <- 1 until row ) {
      for(j <- 1 until column) {
        nodes += 1
        node += nodeFactory.create(nodes,Point3D(i * distance ,j * distance ,z),devs,nodeProto)
      }
    }
    world clear()
    world ++ node
  }
}
