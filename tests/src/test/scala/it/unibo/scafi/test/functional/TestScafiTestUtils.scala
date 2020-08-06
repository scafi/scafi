package it.unibo.scafi.test.functional

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.utils.StatisticsUtils.stdDev
import org.scalatest._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class TestScafiTestUtils extends FlatSpec {
  import ScafiTestUtils._


  val ManhattanNet = new ItWord

  ManhattanNet should "Support every possible detachment" in {
    val net: Network with SimulatorOps = manhattanNet(
      side = 3,
      northWestDetached = true,
      northEastDetached = true,
      southEastDetached = true,
      southWestDetached = true)

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

}
