/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    // ARRANGE
    import node._
    // ACT
    implicit val endNet = runProgram { minHood{1} } (net)
    // ASSERT
    assertForAllNodes[Int]((_,value) => value==1, okWhenNotComputed = true)
  }

  it should "evaluate a string constant field" in new SimulationContextFixture {
    // ACT
    implicit val endNet = runProgram { "XXX" } (net)
    // ASSERT
    assertForAllNodes[String]((_,value) => value=="XXX", okWhenNotComputed = true)
  }

}
