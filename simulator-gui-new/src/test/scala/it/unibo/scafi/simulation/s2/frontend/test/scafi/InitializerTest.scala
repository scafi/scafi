package it.unibo.scafi.simulation.s2.frontend.test.scafi

import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiWorldInformation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.{ScafiWorldInitializer, scafiWorld}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/**
  * a test used to check the correctness of scafi world initializer
  */
class InitializerTest extends AnyFunSpec with Matchers{
  private val checkThat = new ItWord
  private val world = scafiWorld
  private val node = 100
  private val width = 10
  private val space = 1
  private val height = 10
  private val aNodeId = 1
  checkThat("random initializer create a random world") {
    world.clear()
    ScafiWorldInitializer.Random(node,width,height).init(ScafiWorldInformation.standard)
    assert(world.nodes.nonEmpty)
    assert(world.nodes.size == node)
    assert(world(aNodeId).get.devices.nonEmpty)
    assert(world.boundary.isEmpty)
  }

  checkThat("grid initializer create a grid like world") {
    world.clear()
    ScafiWorldInitializer.Grid(space,width,height).init(ScafiWorldInformation.standard)
    assert(world.nodes.nonEmpty)
    assert(world.nodes.size == width * height)
    assert(world(aNodeId).get.devices.nonEmpty)
    assert(world.boundary.isEmpty)
  }
}
