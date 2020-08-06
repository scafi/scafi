package it.unibo.scafi.test.functional.stdlib

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
class TestBlockT extends FlatSpec{
  import ScafiTestUtils._

  val Block_T = new ItWord

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps = ScafiTestUtils.manhattanNet(side = 3, southEastDetached = true)
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BuildingBlocks

  def unitaryDecay: Int => Int = _ - 1
  def halving: Int => Int = _ / 2

  Block_T should "support T with unitary decay and 0 floor value" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Int = T(10, 0, unitaryDecay)
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      0, 0, 0,
      0, 0, 0,
      0, 0, 0
    )).toMap)(net)
  }

  Block_T should "T should initialize as specified" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Int = T(10, 0, identity[Int])
    }, ntimes = fewRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      10, 10, 10,
      10, 10, 10,
      10, 10, 10
    )).toMap)(net)
  }

  Block_T should "T should be consistent in intermediate tests" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): (Int, Int) = (
        T(manyManyRounds, 0, unitaryDecay),
        //this can be seen as a round counter
        rep(0)(_ + 1)
      )
    }, ntimes = someRounds)(net)

    assert(net.valueMap[(Int, Int)]().forall { case (_, (todo: Int, done: Int)) => todo + done == manyManyRounds })
  }

  Block_T should "support T with unitary decay and custom floor value" in new SimulationContextFixture {
    val floorValue = 1
    exec(new TestProgram {
      override def main(): Int = T(10, floorValue, unitaryDecay)
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
      override def main(): Int = T(10, floorValue, unitaryDecay)
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      floorValue, floorValue, floorValue,
      floorValue, floorValue, floorValue,
      floorValue, floorValue, floorValue
    )).toMap)(net)
  }

  Block_T should("support timer operation") in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Int = timer(10)
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
      override def main(): Int = limitedMemory(value, expValue, manyManyRounds)._1
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
      override def main(): Int = limitedMemory(value, expValue, 10)._1
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
      override def main(): Int = sharedTimerWithDecay(1, 1)
    }, ntimes = manyManyRounds)(net)

    //standard deviation inside the same group should be low
    assert(stdDev(net.valueMap[Int]().filterKeys(_ != 8).values) < maxStdDev)
  }

  Block_T should("T should restart after branch switch") in new SimulationContextFixture {
    var count = 0

    exec(new TestProgram {
      net.addSensor[Boolean]("sensor", false)
      net.chgSensorValue("sensor", Set(0), true)

      override def main(): Unit =
        branch(sense[Boolean]("sensor"))
        {
          if (rep(0)(_ + 1) == 10) {
            net.chgSensorValue[Boolean]("sensor", Set(0), false)
          }
        }
        {
          if (T(10, 0, unitaryDecay) == 0) {
            net.chgSensorValue[Boolean]("sensor", Set(0), true)
            count = count + 1
          }
        }
    }, ntimes = someRounds)(net)

    assert(count > 20)
  }
}
