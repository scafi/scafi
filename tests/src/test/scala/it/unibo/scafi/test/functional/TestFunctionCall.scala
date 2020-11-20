/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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

  private[this] trait Node extends ScafiStandardAggregateProgram {
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
    var (endNet, _) = runProgram({
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
    }, ntimes = 1000)(net)._1
    assertNetworkValues(expectedNet)(endNet)

    def aggregateMethodForObstacles = () => aggregate { -numOfNeighbors }
    def aggregateMethodForNormalNodes = () => aggregate { numOfNeighbors }
    endNet = runProgram({
      mux(isObstacle)(aggregateLambdaForObstacles)(aggregateLambdaForNormalNodes)()
    }, ntimes = 1000)(net)._1
    assertNetworkValues(expectedNet)(endNet)
  }

  AggregateFunctionCall should "work, e.g., when calculating hop gradient" in new SimulationContextFixture {
    // ARRANGE
    import node._
    val max = Int.MaxValue
    // ACT
    implicit val (endNet, _) = runProgram({
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
