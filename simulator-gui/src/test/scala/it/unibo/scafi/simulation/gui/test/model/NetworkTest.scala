package it.unibo.scafi.simulation.gui.test.model

import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld.NeighbourChanged
import it.unibo.scafi.simulation.gui.model.space.Point
import it.unibo.scafi.simulation.gui.test.help.platform
import org.scalatest.{FunSpec, Matchers}

class NetworkTest extends FunSpec with Matchers {
  val checkThat = new ItWord
  val node = 100
  val zero = Point.ZERO
  (0 until node) foreach {x => platform.insertNode(new platform.NodeBuilder(x,zero))}
  val observer = platform.createObserver(Set(NeighbourChanged))

  checkThat("i can change the node neighbour") {
    platform.network.setNeighbours(0,Set(1,2,3))
    assert(platform.network.neighbours().nonEmpty)
    assert(platform.network.neighbours(0).nonEmpty)
    assert(observer.nodeChanged().contains(0))
  }
}
