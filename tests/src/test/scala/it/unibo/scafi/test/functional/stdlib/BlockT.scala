package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.utils.StatisticsUtils._
import org.scalatest._

/*

Not yet tested:
- cyclicTimerWithDecay
- clock
- impulsesEvery
 */
class BlockT extends FlatSpec{
  import ScafiTestUtils._

  val Block_T = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.0
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy,
        mapPos = (a,b,px,py) => if(a==2 && b==2) (100,100) else (px,py)), rng = 1.5))
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BuildingBlocks

  def SetupNetwork(n: Network with SimulatorOps): FunctionalTestIncarnation.Network with FunctionalTestIncarnation.SimulatorOps = {
    n
  }

  def unitaryDecay: Int => Int = _ - 1
  def halving: Int => Int = _ / 2

  Block_T should "support T with unitary decay and 0 floor value" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = T(10, 0, unitaryDecay)
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      0, 0, 0,
      0, 0, 0,
      0, 0, 0
    )).toMap)(net)
  }

  Block_T should "support T with unitary decay and custom floor value" in new SimulationContextFixture {
    val floorValue = 1
    exec(new TestProgram {
      override def main(): Any = T(10, floorValue, unitaryDecay)
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      floorValue, floorValue, floorValue,
      floorValue, floorValue, floorValue,
      floorValue, floorValue, floorValue
    )).toMap)(net)
  }

  Block_T should "support T with unitary decay and negative floor value" in new SimulationContextFixture {
    val floorValue: Int = -10
    exec(new TestProgram {
      override def main(): Any = T(10, floorValue, unitaryDecay)
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      floorValue, floorValue, floorValue,
      floorValue, floorValue, floorValue,
      floorValue, floorValue, floorValue
    )).toMap)(net)
  }

  Block_T should("support timer operation") in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = timer(10)
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      0, 0, 0,
      0, 0, 0,
      0, 0, 0
    )).toMap)(net)
  }

  Block_T should("support limitedMemory - without expiration") in new SimulationContextFixture {
    val value: Int = 10
    val expValue: Int = -1
    exec(new TestProgram {
      override def main(): Any = limitedMemory(value, expValue, manyManyRounds)._1
    }, ntimes = fewRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      value, value, value,
      value, value, value,
      value, value, value
    )).toMap)(net)
  }

  Block_T should("support limitedMemory - with expiration") in new SimulationContextFixture {
    val value: Int = 10
    val expValue: Int = -1
    exec(new TestProgram {
      override def main(): Any = limitedMemory(value, expValue, 10)._1
    }, ntimes = manyRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      expValue, expValue, expValue,
      expValue, expValue, expValue,
      expValue, expValue, expValue
    )).toMap)(net)
  }

  Block_T should("support sharedTimerWithDecay") in new SimulationContextFixture {
    val maxStdDev: Int = 2
    exec(new TestProgram {
      override def main(): Any = sharedTimerWithDecay(1, 1)
    }, ntimes = manyManyRounds)(net)

    //standard deviation inside the same group should be low
    assert(stdDev((0 to 7).map(i => net.exports()(i).get.root().asInstanceOf[Int])) < maxStdDev)
  }
}
