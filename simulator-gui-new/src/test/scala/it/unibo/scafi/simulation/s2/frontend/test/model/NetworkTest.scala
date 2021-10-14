package it.unibo.scafi.simulation.s2.frontend.test.model

import it.unibo.scafi.simulation.s2.frontend.model.common.network.ConnectedWorld.NeighbourChanged
import it.unibo.scafi.simulation.s2.frontend.test.help.platform
import it.unibo.scafi.space.Point3D
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class NetworkTest extends AnyFunSpec with Matchers {
  private val checkThat = new ItWord
  private val node = 100
  private val zero = Point3D.Zero
  (0 until node) foreach {x => platform.insertNode(new platform.NodeBuilder(x,zero))}
  val observer = platform.createObserver(Set(NeighbourChanged))

  checkThat("i can change the node neighbour") {
    platform.network.setNeighbours(0,Set(1,2,3))
    assert(platform.network.neighbours().nonEmpty)
    assert(platform.network.neighbours(0).nonEmpty)
    assert(observer.nodeChanged().contains(0))
  }
}
