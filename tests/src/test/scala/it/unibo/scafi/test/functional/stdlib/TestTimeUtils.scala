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
    val net: Network with SimulatorOps = manhattanNet(detachedNodesCoords = Set((2,2)))
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BuildingBlocks

  val unitaryDecay: Int => Int = _ - 1
  val halving: Int => Int = _ / 2

  Time_Utils should "support timerLocalTime" in new SimulationContextFixture {
    val testProgram: TestProgram = new TestProgram {
      override def main(): Any = timerLocalTime(0.25 second)
    }

    exec(testProgram, ntimes = someRounds)(net)
    assert(net.valueMap[Long]().forall(e => e._2 > 0))

    exec(testProgram, ntimes = manyManyRounds)(net)
    assert(net.valueMap[Long]().forall(e => e._2 == 0))
  }

  Time_Utils should "support impulsesEvery" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = rep(0)(_ + (if (impulsesEvery(1 nanosecond)) 1 else 0) )
    }, ntimes = manyManyRounds)(net)

    assert(net.valueMap[Int]().forall(e => e._2 > 0))
  }

  Time_Utils should "support sharedTimer" in new SimulationContextFixture {
    val maxStdDev: Int = 10

    val testProgram: TestProgram = new TestProgram {
      override def main(): Any = sharedTimer(1 seconds)
    }

    exec(testProgram, ntimes = someRounds)(net)
    assert(stdDev(net.valueMap[FiniteDuration]().filterKeys(_ != 8).values.map(_.toMillis)) < maxStdDev)

    exec(testProgram, ntimes = manyManyRounds)(net)
    assert(stdDev(net.valueMap[FiniteDuration]().filterKeys(_ != 8).values.map(_.toMillis)) < maxStdDev)
  }

  Time_Utils should "support recentlyTrue" in new SimulationContextFixture {
    net.addSensor[Boolean]("rtSense", false)

    val testProgram: TestProgram = new TestProgram {
      override def main(): Boolean =
        recentlyTrue(0.05 second, cond = sense[Boolean]("rtSense"))
    }

    exec(testProgram, ntimes = someRounds)(net)
    assertNetworkValues((0 to 8).zip(List(
      false, false, false,
      false, false, false,
      false, false, false
    )).toMap)(net)

    net.chgSensorValue("rtSense", Set(0), value = true)
    exec(testProgram, ntimes = someRounds)(net)
    assertNetworkValues((0 to 8).zip(List(
      true, false, false,
      false, false, false,
      false, false, false
    )).toMap)(net)

    net.chgSensorValue("rtSense", Set(0), value = false)
    exec(testProgram, ntimes = fewRounds)(net)
    assertNetworkValues((0 to 8).zip(List(
      true, false, false,
      false, false, false,
      false, false, false
    )).toMap)(net)

    net.chgSensorValue("rtSense", Set(0), value = false)
    exec(testProgram, ntimes = manyManyRounds)(net)
    assertNetworkValues((0 to 8).zip(List(
      false, false, false,
      false, false, false,
      false, false, false
    )).toMap)(net)
  }

  Time_Utils should("support evaporation") in new SimulationContextFixture {
    val ceiling: Int = someRounds

    val testProgram: TestProgram = new TestProgram {
      override def main(): (Int, String) = evaporation(ceiling, "hello")
    }

    exec(testProgram, ntimes = manyRounds)(net)
    assert(net.valueMap[(Int, String)]().forall {
      case (_, (n, "hello")) if n > 0 => true
      case _ => false
    })

    exec(testProgram, ntimes = manyManyRounds * 3)(net)
    assertNetworkValues((0 to 8).zip(List(
      (0, "hello"), (0, "hello"), (0, "hello"),
      (0, "hello"), (0, "hello"), (0, "hello"),
      (0, "hello"), (0, "hello"), (0, "hello")
    )).toMap)(net)
  }

  Time_Utils should("support evaporation - with custom decay") in new SimulationContextFixture {
    val ceiling: Int = 1000000

    val testProgram: TestProgram = new TestProgram {
      override def main(): (Int, String) = evaporation(ceiling, halving,"hello")
    }

    exec(testProgram, ntimes = fewRounds)(net)
    assert(net.valueMap[(Int, String)]().forall {
      case (_, (n, "hello")) if n > 0 => true
      case _ => false
    })

    exec(testProgram, ntimes = manyManyRounds)(net)
    net.valueMap[(Int, String)]().forall {
      case (_, (0, "hello")) => true
      case _ => false
    }

    assertNetworkValues((0 to 8).zip(List(
      (0, "hello"), (0, "hello"), (0, "hello"),
      (0, "hello"), (0, "hello"), (0, "hello"),
      (0, "hello"), (0, "hello"), (0, "hello")
    )).toMap)(net)
  }
}
