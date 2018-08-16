package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.{DeviceSeed, WorldInitializer, WorldSeed}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiSeed
import it.unibo.scafi.simulation.gui.model.core.Shape
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.model.space.Point3D

import scala.util.{Random => RandomGenerator}

/**
  * the interface of a scafi world initializer
  */
trait ScafiWorldInitializer extends WorldInitializer[ScafiSeed]
object ScafiWorldInitializer {
  type SEED = ScafiSeed
  val standardShape = Rectangle(2,2)
  /**
    * create a random world
    * @param node the node number
    * @param width the max width where node put in the world
    * @param height the max height where node put in the world
    */
  case class Random(node : Int,
                    width : Int,
                    height : Int) extends ScafiWorldInitializer {
    override def init(seed : SEED): Unit = {
      val r = new RandomGenerator()
      //all nodes on the same 2d planes
      val z = 0
      scafiWorld clear()
      (0 until node) foreach {x => scafiWorld.insertNode(new scafiWorld.NodeBuilder(x,Point3D(r.nextInt(width),r.nextInt(height),z),seed.shape,seed.deviceSeed.devices.toList))}
      scafiWorld.boundary = seed.boundary
    }
  }

  /**
    * create a grid like world
    * @param row number of row
    * @param column number of column
    * @param space space between node
    */
  case class Grid(row : Int,
                  column: Int,
                  space : Int) extends ScafiWorldInitializer {
    override def init(seed : SEED): Unit = {
      val z = 0
      var nodes = 0;
      scafiWorld clear()
      for(i <- 1 to row) {
        for(j <- 1 to column) {
          nodes += 1
          scafiWorld.insertNode(new scafiWorld.NodeBuilder(nodes,Point3D(i * space,j * space,z),seed.shape,seed.deviceSeed.devices.toList))
        }
      }
      scafiWorld.boundary = seed.boundary
    }
  }
}
