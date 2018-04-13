package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld.{SensorType, in}
import it.unibo.scafi.simulation.gui.incarnation.scafi.SimpleScafiWorld
import it.unibo.scafi.simulation.gui.launcher.SensorName._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.space.Point3D

import scala.util.Random
//TODO COMPLETE
object WorldConfig {
  import it.unibo.scafi.simulation.gui.incarnation.scafi.SimpleScafiWorld._

  val world = SimpleScafiWorld

  world.boundary = None

  def putBoundary(s : world.S) { world.boundary = Some(new ShapeBoundary(s))}
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
    * @param sensorType the type of sensor
    * @tparam V the type of value
    * @return the devices created
    */
  def dev[V](n : Name, value : V = true, sensorType : SensorType) : DEVICE = deviceFactory.create(n.name,new ExternalDevicePrototype(value,sensorType))

  /**
    * used to create a prototype
    * @param shape the shape of node
    * @return the prototype of node
    */
  def NodePrototype(shape : S) : NODE_PROTOTYPE = new ExternalNodePrototype(Some(shape))



  /**
    * initialize a world 2D in a randomize way
    * @param number the number of element
    */
  def randomize2D(number : Int,boundary : Option[Shape2D]): Unit = {
    val r = new Random()
    val maxPoint = 1000
    //all nodes on the same 2d planes
    val z = 0
    val node : Set[NODE] = boundary match {
      case Some(Rectangle(w,h,_)) => {
        ((0 to number) map {
          nodeFactory.create(_,Point3D(r.nextInt(w.toInt),r.nextInt(h.toInt),z),devs,nodeProto)
        } toSet)
      }
      case _ => {
        ((0 to number) map {
          nodeFactory.create(_,Point3D(r.nextInt(maxPoint),r.nextInt(maxPoint),z),devs,nodeProto)
        } toSet)
      }
    }
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
    println(node)
    world clear()
    world ++ node
  }
}
