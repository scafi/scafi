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

class TestFunctionCall extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val AggregateFunctionCall = new ItWord

  private[this] trait SimulationContextFixture {
    implicit val node = new Node {
      override type MainResult = Any
      override def main() = ???
    }

    val net: Network with SimulatorOps =
      simulatorFactory.gridLike(GridSettings(nrows = 6, ncols = 6, stepx = 1, stepy = 1, tolerance = 0), rng = 1.1)
    net.addSensor(name = "source", value = false)
    net.chgSensorValue(name = "source", ids = Set(2), value = true)
    net.addSensor(name = "obstacle", value = false)
    net.chgSensorValue(name = "obstacle", ids = Set(21,22,27,28,33), value = true)
  }
  // NETWORK (devices by their ids)
  //  0  1  2  3  4  5
  //  6  7  8  9 10 11
  // 12 13 14 15 16 17
  // 18 19 20 21 22 23
  // 24 25 26 27 28 29
  // 30 31 32 33 34 35
  // For each device, its neighbors are the direct devices at the top/bottom/left/right

  private[this] trait Node extends AggregateProgram {
    def isObstacle = sense[Boolean]("obstacle")
    def isSource = sense[Boolean]("source")

    def hopGradient(source: Boolean): Int = {
      rep(Double.PositiveInfinity){
        hops => {
          mux(source){ 0.0 } { 1+minHood(nbr{ hops }) }
        }
      }.toInt
      // NOTE 1: Double.PositiveInfinity + 1 = Double.PositiveInfinity
      // NOTE 2: Double.PositiveInfinity.toInt = Int.MaxValue
    }

    def numOfNeighbors: Int = foldhood(0)(_+_)(nbr { 1 })
  }

  AggregateFunctionCall should "support restriction, e.g., while counting neighbors" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    var endNet = runProgram({
      mux(isObstacle)(() => aggregate { -numOfNeighbors } )(() => aggregate { numOfNeighbors })()
    }, ntimes = 1000)(net)
    // ASSERT
    // Expected network: note how the number of neighbors for "obstacle" devices are restricted
    var expectedNet = (0 to 35).zip(List(
      3, 4, 4,  4,  4, 3,
      4, 5, 5,  5,  5, 4,
      4, 5, 5,  4,  4, 4,
      4, 5, 4, -3, -3, 3,
      4, 5, 4, -4, -3, 3,
      3, 4, 3, -2,  2, 3
    )).toMap
    assertNetworkValues(expectedNet)(endNet)

    val aggregateLambdaForObstacles = () => aggregate { -numOfNeighbors }
    val aggregateLambdaForNormalNodes = () => aggregate { numOfNeighbors }
    endNet = runProgram({
      mux(isObstacle)(aggregateLambdaForObstacles)(aggregateLambdaForNormalNodes)()
    }, ntimes = 1000)(net)
    assertNetworkValues(expectedNet)(endNet)

    def aggregateMethodForObstacles = () => aggregate { -numOfNeighbors }
    def aggregateMethodForNormalNodes = () => aggregate { numOfNeighbors }
    endNet = runProgram({
      mux(isObstacle)(aggregateLambdaForObstacles)(aggregateLambdaForNormalNodes)()
    }, ntimes = 1000)(net)
    assertNetworkValues(expectedNet)(endNet)
  }

  AggregateFunctionCall should "work, e.g., when calculating hop gradient" in new SimulationContextFixture {
    // ARRANGE
    import node._
    val max = Int.MaxValue
    // ACT
    implicit val endNet = runProgram({
      mux(isObstacle)(() => aggregate { max } )(() => aggregate { hopGradient(isSource) })()
    }, ntimes = 1000)(net)
    // ASSERT
    assertNetworkValues((0 to 35).zip(List(
      2, 1, 0,   1,   2, 3,
      3, 2, 1,   2,   3, 4,
      4, 3, 2,   3,   4, 5,
      5, 4, 3, max, max, 6,
      6, 5, 4, max, max, 7,
      7, 6, 5, max,   9, 8
    )).toMap)
  }
}
