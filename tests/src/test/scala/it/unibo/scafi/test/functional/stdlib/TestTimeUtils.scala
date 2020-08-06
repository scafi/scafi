package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.utils.StatisticsUtils.stdDev
import org.scalatest._

import scala.concurrent.duration._

/*
Still to test:
- cyclicFunction
- cyclicFunctionWithDecay
 */
class TestTimeUtils extends FlatSpec{
  import ScafiTestUtils._

  val Time_Utils = new ItWord

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps = manhattanNet(side = 3, southWestDetached = true)
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BuildingBlocks

  def unitaryDecay: Int => Int = _ - 1
  def halving: Int => Int = _ / 2

  Time_Utils should "support timerLocalTime" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = timerLocalTime(1 millisecond)
    }, ntimes = manyManyRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      0, 0, 0,
      0, 0, 0,
      0, 0, 0
    )).toMap)(net)
  }

  Time_Utils should "support impulsesEvery" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = rep(0)(_ + (if (impulsesEvery(1 nanosecond)) 1 else 0) )
    }, ntimes = manyManyRounds)(net)

    assert(net.valueMap[Int]().forall(e => e._2 > 0))
  }

  Time_Utils should "support sharedTimer" in new SimulationContextFixture {
    val maxStdDev: Int = 1
    exec(new TestProgram {
      override def main(): Any = sharedTimer(10 seconds)
    }, ntimes = manyManyRounds)(net)

    assert(stdDev(net.valueMap[FiniteDuration]().filterKeys(_ != 8).values.map(_.toMillis)) < maxStdDev)
  }

  Time_Utils should "support recentlyTrue" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = (
        recentlyTrue(1 second, cond = true),
        recentlyTrue(1 seconds, cond = false)
      )
    }, ntimes = fewRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (true, false), (true, false), (true, false),
      (true, false), (true, false), (true, false),
      (true, false), (true, false), (true, false)
    )).toMap)(net)
  }

  Time_Utils should("support evaporation") in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = evaporation(10, "hello")
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      ("hello", 0), ("hello", 0), ("hello", 0),
      ("hello", 0), ("hello", 0), ("hello", 0),
      ("hello", 0), ("hello", 0), ("hello", 0)
    )).toMap)(net)
  }

  Time_Utils should("support evaporation - with custom decay") in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = evaporation(1000000, halving,"hello")
    }, ntimes = someRounds)(net)
    
    assertNetworkValues((0 to 8).zip(List(
      ("hello", 0), ("hello", 0), ("hello", 0),
      ("hello", 0), ("hello", 0), ("hello", 0),
      ("hello", 0), ("hello", 0), ("hello", 0)
    )).toMap)(net)
  }
}
