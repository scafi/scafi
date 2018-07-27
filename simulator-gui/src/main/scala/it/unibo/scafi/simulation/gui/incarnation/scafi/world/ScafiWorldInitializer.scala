package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.model.core.{Shape, World}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle, Rectangle}
import it.unibo.scafi.simulation.gui.model.space.Point3D

import scala.util.{Random => RandomGenerator}

/**
  * the interface of a scafi world initializer
  */
trait ScafiWorldInitializer {
  /**
    * a seed used to create sensor
    * @return
    */
  def sensors : ScafiSensorSeed
  /**
    * init a scafi world
    */
  def init()
}
object ScafiWorldInitializer {
  /**
    * create a random world
    * @param node the node number
    * @param width the max width where node put in the world
    * @param height the max height where node put in the world
    * @param sensors the sensor seed of each node
    * @param boundary world boundary
    */
  case class Random(node : Int,
                    width : Int,
                    height : Int,
                    sensors : ScafiSensorSeed = ScafiSensorSeed.standardSeed,
                    nodeShape : Shape = Circle(3),
                    boundary : Option[scafiWorld.B] = None) extends ScafiWorldInitializer {
    override def init(): Unit = {
      val r = new RandomGenerator()
      //all nodes on the same 2d planes
      val z = 0
      scafiWorld clear()
      (0 until node) foreach {x => scafiWorld.insertNode(new scafiWorld.NodeBuilder(x,Point3D(r.nextInt(width),r.nextInt(height),z),Some(nodeShape),sensors.sensor.toList))}
      scafiWorld.boundary = boundary
    }
  }

  /**
    * create a grid like world
    * @param row number of row
    * @param column number of column
    * @param space space between node
    * @param sensors sensor seed
    * @param nodeShape node shape
    * @param boundary world boundary
    */
  case class Grid(row : Int,
                  column: Int,
                  space : Int,
                  sensors : ScafiSensorSeed = ScafiSensorSeed.standardSeed,
                  nodeShape : Shape = Circle(3),
                  boundary : Option[scafiWorld.B] = None) extends ScafiWorldInitializer {
    override def init(): Unit = {
      val z = 0
      var nodes = 0;
      scafiWorld clear()
      for(i <- 1 to row) {
        for(j <- 1 to column) {
          nodes += 1
          scafiWorld.insertNode(new scafiWorld.NodeBuilder(nodes,Point3D(i * space,j * space,z),Some(nodeShape),sensors.sensor.toList))
        }
      }
      scafiWorld.boundary = boundary
    }
  }
}
