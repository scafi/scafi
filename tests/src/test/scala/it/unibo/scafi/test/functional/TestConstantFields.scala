/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
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
      simulatorFactory.gridLike(GridSettings(nrows = 10, ncols = 10, stepx = 1, stepy = 1, tolerance = 100), rng = 1)
  }

  it should "evaluate an int constant field" in new SimulationContextFixture {
    import node._

    implicit val (endNet, _) = runProgram { minHood{1} } (net)

    assertForAllNodes[Int]((_,value) => value==1, okWhenNotComputed = true)
  }

  it should "evaluate a string constant field" in new SimulationContextFixture {
    implicit val (endNet, _) = runProgram { "XXX" } (net)

    assertForAllNodes[String]((_,value) => value=="XXX", okWhenNotComputed = true)
  }

}
