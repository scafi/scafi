package it.unibo.scafi.test.functional

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._

class TestScafiTestUtils extends FlatSpec {
  import ScafiTestUtils._

  val ManhattanNet = new ItWord

  ManhattanNet should "Support every possible detachment" in {
    val net: Network with SimulatorOps = manhattanNet(
      detachedNodesCords = Set((0,0), (2,0), (0,2), (2,2))
    )

    assert(net.neighbourhood(0).isEmpty)
    assert(net.neighbourhood(1) == Set(3,4,5))
    assert(net.neighbourhood(2).isEmpty)
    assert(net.neighbourhood(3) == Set(1,4,7))
    assert(net.neighbourhood(4) == Set(1,3,5,7))
    assert(net.neighbourhood(5) == Set(1,4,7))
    assert(net.neighbourhood(6).isEmpty)
    assert(net.neighbourhood(7) == Set(3,4,5))
    assert(net.neighbourhood(8).isEmpty)
  }
  ManhattanNet should "Support south detached" in {
    val net: Network with SimulatorOps = manhattanNet(
      detachedNodesCords = Set((0,2), (2,2))
    )

    assert(net.neighbourhood(0) == Set(1,3,4))
    assert(net.neighbourhood(1) == Set(0,2,3,4,5))
    assert(net.neighbourhood(2) == Set(1,4,5))
    assert(net.neighbourhood(3) == Set(0,1,4,7))
    assert(net.neighbourhood(4) == Set(0,1,2,3,5,7))
    assert(net.neighbourhood(5) == Set(1,2,4,7))
    assert(net.neighbourhood(6).isEmpty)
    assert(net.neighbourhood(7) == Set(3,4,5))
    assert(net.neighbourhood(8).isEmpty)
  }


  ManhattanNet should "Support south east detached" in {
    val net: Network with SimulatorOps = manhattanNet(
      detachedNodesCords = Set((2,2))
    )

    assert(net.neighbourhood(0) == Set(1,3,4))
    assert(net.neighbourhood(1) == Set(0,2,3,4,5))
    assert(net.neighbourhood(2) == Set(1,4,5))
    assert(net.neighbourhood(3) == Set(0,1,4,6,7))
    assert(net.neighbourhood(4) == Set(0,1,2,3,5,6,7))
    assert(net.neighbourhood(5) == Set(1,2,4,7))
    assert(net.neighbourhood(6) == Set(3,4,7))
    assert(net.neighbourhood(7) == Set(3,4,5,6))
    assert(net.neighbourhood(8).isEmpty)
  }

  ManhattanNet should "Support south west detached" in {
    val net: Network with SimulatorOps = manhattanNet(detachedNodesCords = Set((0,2)))

    assert(net.neighbourhood(0) == Set(1,3,4))
    assert(net.neighbourhood(1) == Set(0,2,3,4,5))
    assert(net.neighbourhood(2) == Set(1,4,5))
    assert(net.neighbourhood(3) == Set(0,1,4,7))
    assert(net.neighbourhood(4) == Set(0,1,2,3,5,7,8))
    assert(net.neighbourhood(5) == Set(1,2,4,7,8))
    assert(net.neighbourhood(6).isEmpty)
    assert(net.neighbourhood(7) == Set(3,4,5,8))
    assert(net.neighbourhood(8) == Set(4,5,7))
  }


}
