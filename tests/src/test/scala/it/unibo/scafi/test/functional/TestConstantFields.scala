package it.unibo.scafi.test.functional

/**
 * Created by: Roberto Casadei
 * Created on date: 30/10/15
 */

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._

class TestConstantFields extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  private[this] trait SimulationContextFixture {
    implicit val node = new AggregateInterpreter {
      override type MainResult = Any
      override def main() = ???
    }
    var net: Network with SimulatorOps =
      simulatorFactory.gridLike(n = 10, m = 10, stepx = 1, stepy = 1, eps = 100, rng = 1)
  }

  it should "evaluate an int constant field" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit val endNet = runProgram { minHood{1} } (net)
    // ASSERT
    assertForAllNodes[Int](_==1, okWhenNotComputed = true)
  }

  it should "evaluate a string constant field" in new SimulationContextFixture {
    // ACT
    implicit val endNet = runProgram { "XXX" } (net)
    // ASSERT
    assertForAllNodes[String](_=="XXX", okWhenNotComputed = true)
  }

}
