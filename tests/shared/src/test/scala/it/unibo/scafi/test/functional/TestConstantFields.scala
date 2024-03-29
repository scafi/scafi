/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestConstantFields extends AnyFlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  private[this] class SimulationContextFixture(seeds: Seeds) {
    implicit val node = new AggregateInterpreter {
      override type MainResult = Any
      override def main() = ???
    }
    val net: Network with SimulatorOps =
      simulatorFactory.gridLike(GridSettings(nrows = 10, ncols = 10, stepx = 1, stepy = 1, tolerance = 100), rng = 1, seeds = seeds)
  }

  for(s <- seeds) {
    val seeds = Seeds(s, s, s)
    behavior of s"Constant fields for $seeds"
    it should behave like behaviours(seeds)
  }

  def behaviours(seeds: Seeds): Unit = {
    it should "evaluate an int constant field" in new SimulationContextFixture(seeds) {
      import node._
      implicit val (endNet, _) = runProgram { minHood { 1 } }(net)
      assertForAllNodes[Int]((_, value) => value == 1, okWhenNotComputed = true)
    }

    it should "evaluate a string constant field" in new SimulationContextFixture(seeds) {
      implicit val (endNet, _) = runProgram { "XXX" }(net)

      assertForAllNodes[String]((_, value) => value == "XXX", okWhenNotComputed = true)
    }
  }

}
