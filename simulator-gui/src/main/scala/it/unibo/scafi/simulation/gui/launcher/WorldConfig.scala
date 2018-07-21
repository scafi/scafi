package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorld
import it.unibo.scafi.simulation.gui.launcher.SensorName._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.model.graphics2D.Shape2D
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorStream
import it.unibo.scafi.simulation.gui.model.space.Point3D

import scala.util.Random
//TODO COMPLETE
object WorldConfig {
  import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorld._

  val world : ScafiWorld.type = ScafiWorld

  world.boundary = None

  def putBoundary(s : world.S) { world.boundary = Some(new ShapeBoundary(s))}

  /**
    * the devices attach on a node
    */
  var devs = Set[DEVICE_PRODUCER]()

  /**
    * function used to create a device
    * @param n the name
    * @param value the value
    * @param sensorStream the type of sensor
    * @tparam V the type of value
    * @return the devices created
    */
  def dev[V](n : Name, value : V = true, sensorStream: SensorStream) : DEVICE_PRODUCER = value match {
    case v : Boolean => LedProducer(n.name,v,sensorStream)
    case v : Double => DoubleSensorValue(n.name,v,sensorStream)
    case _ => GeneralSensorProducer(n.name,value,sensorStream)
  }

  /**
    * initialize a world 2D in a randomize way
    * @param number the number of element
    */
  def randomize2D(number : Int,boundary : Option[Shape2D]): Unit = {
    val r = new Random()
    val maxPoint = 1000
    //all nodes on the same 2d planes
    val z = 0
    world clear()
    boundary match {
      case Some(Rectangle(w,h,_)) => {
        ((0 to number) foreach {
          x => world.insertNode(new world.NodeBuilder(x,Point3D(r.nextInt(w.toInt),r.nextInt(h.toInt),z),Some(Rectangle(2,2)),devs.toList))
        })
      }
      case _ => {
        ((0 to number) foreach {
          x => world.insertNode(new world.NodeBuilder(x,Point3D(r.nextInt,r.nextInt,z),Some(Rectangle(2,2)),devs.toList))
        } )
      }
    }
  }

  def gridLike2D(row : Int, column : Int, distance : Double): Unit = {
    /*var node : Set[NODE] = Set()
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
    world ++ node*/
  }
}
