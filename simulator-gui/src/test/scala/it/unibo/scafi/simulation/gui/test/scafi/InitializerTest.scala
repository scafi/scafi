package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiWorldInformation
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiWorldInitializer, scafiWorld}
import org.scalatest.{FunSpec, Matchers}

/**
  * a test used to check the correctness of scafi world initializer
  */
class InitializerTest extends FunSpec with Matchers{
  val checkThat = new ItWord

  val world = scafiWorld
  val node = 100
  val width = 10
  val space = 1
  val height = 10
  val aNodeId = 1
  checkThat("random initializer create a random world") {
    world.clear()
    val init = ScafiWorldInitializer.Random(node,width,height).init(ScafiWorldInformation.standard)
    assert(world.nodes.nonEmpty)
    assert(world.nodes.size == node)
    assert(world(aNodeId).get.devices.nonEmpty)
    assert(world.boundary.isEmpty)
  }

  checkThat("grid initializer create a grid like workd") {
    world.clear()
    val init = ScafiWorldInitializer.Grid(width,height,space).init(ScafiWorldInformation.standard)
    assert(world.nodes.nonEmpty)
    assert(world.nodes.size == width * height)
    assert(world(aNodeId).get.devices.nonEmpty)
    assert(world.boundary.isEmpty)
  }
}
