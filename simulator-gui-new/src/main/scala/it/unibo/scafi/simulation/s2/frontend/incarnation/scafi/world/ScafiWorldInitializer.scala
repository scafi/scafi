package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world

import it.unibo.scafi.simulation.s2.frontend.configuration.information.WorldInitializer
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiWorldInformation
import it.unibo.scafi.space.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.space.Point3D

import scala.util.{Random => RandomGenerator}

/**
 * the interface of a scafi world initializer
 */
trait ScafiWorldInitializer extends WorldInitializer[ScafiWorldInformation] {
  def size: (Double, Double)
}
object ScafiWorldInitializer {
  type INFO = ScafiWorldInformation
  val standardShape: Rectangle = Rectangle(2, 2)

  /**
   * create a random world
   *
   * @param node
   *   the node number
   * @param width
   *   the max width where node put in the world
   * @param height
   *   the max height where node put in the world
   */
  case class Random(node: Int, width: Int, height: Int) extends ScafiWorldInitializer {
    override def init(worldInfo: INFO): Unit = {
      scafiWorld.boundary = worldInfo.boundary
      val r = new RandomGenerator()
      // all nodes on the same 2d planes
      val z = 0
      scafiWorld.clear()
      for (i <- 0 until node) {
        val randomPoint = Point3D(r.nextInt(width), r.nextInt(height), z)
        val addPoint = worldInfo.boundary match {
          case Some(bound) => bound.accept(randomPoint)
          case _ => true
        }
        if (addPoint)
          scafiWorld.insertNode(
            new scafiWorld.NodeBuilder(i, randomPoint, worldInfo.shape, worldInfo.deviceProducers.toList)
          )
      }
    }

    override def size: (Double, Double) = (width, height)
  }

  /**
   * create a grid like world
   *
   * @param space
   *   number of row
   * @param row
   *   number of column
   * @param column
   *   space between node
   */
  case class Grid(space: Int, row: Int, column: Int) extends ScafiWorldInitializer {
    override def init(worldInfo: INFO): Unit = {
      scafiWorld.boundary = worldInfo.boundary
      val z = 0
      var nodes = 0
      scafiWorld.clear()
      for (i <- 1 to row) {
        for (j <- 1 to column) {
          nodes += 1
          // - space is used because i and j start with 1 and not with 0
          val point = Point3D((i * space) - space, (j * space) - space, z)
          val addPoint = worldInfo.boundary match {
            case Some(bound) => bound.accept(point)
            case _ => true
          }
          if (addPoint)
            scafiWorld.insertNode(
              new scafiWorld.NodeBuilder(nodes, point, worldInfo.shape, worldInfo.deviceProducers.toList)
            )
        }
      }
    }

    override def size: (Double, Double) = (row * space - space, column * space - space)
  }

  import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
  case class Fixed(nodes: Set[(ID, Point2D)]) extends ScafiWorldInitializer {
    override def init(worldInfo: INFO): Unit = {
      scafiWorld.boundary = worldInfo.boundary
      scafiWorld.clear()
      nodes.foreach(node =>
        scafiWorld.insertNode(
          new scafiWorld.NodeBuilder(node._1, node._2, worldInfo.shape, worldInfo.deviceProducers.toList)
        )
      )
    }

    override def size: (Double, Double) = (
      nodes.map(_._2.x).max - nodes.map(_._2.x).min + 10,
      nodes.map(_._2.y).max - nodes.map(_._2.y).min + 10
    )
  }
}
