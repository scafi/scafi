package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.utils.StatisticsUtils.stdDev
import org.scalatest._

import scala.concurrent.duration._
import scala.math.Numeric.BigDecimalAsIfIntegral.mkOrderingOps

/*
Still to test:
- cyclicFunction
- cyclicFunctionWithDecay
 */
class TestTimeUtils extends FlatSpec{
  import ScafiTestUtils._

  val Time_Utils = new ItWord

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps = manhattanNet(side = 3, southEastDetached = true)
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BuildingBlocks

  def unitaryDecay: Int => Int = _ - 1
  def halving: Int => Int = _ / 2

  Time_Utils should "support timerLocalTime" in new SimulationContextFixture {
    val testProgram: TestProgram = new TestProgram {
      override def main(): Any = timerLocalTime(1 second)
    }

    exec(testProgram, ntimes = someRounds)(net)
    net.valueMap[Long]().forall(e => e._2 > 0)

    exec(testProgram, ntimes = manyManyRounds)(net)
    net.valueMap[Long]().forall(e => e._2 == 0)
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

    //initially the sensor is set to false -> everyone is false
    exec(testProgram, ntimes = someRounds)(net)
    assertNetworkValues((0 to 8).zip(List(
      false, false, false,
      false, false, false,
      false, false, false
    )).toMap)(net)

    //the sensor is set to true on node 0 -> node 0 should be true
    net.chgSensorValue("rtSense", Set(0), value = true)
    exec(testProgram, ntimes = someRounds)(net)
    assertNetworkValues((0 to 8).zip(List(
      true, false, false,
      false, false, false,
      false, false, false
    )).toMap)(net)

    //sensor is set to false on node 0 -> should still be true for some time
    net.chgSensorValue("rtSense", Set(0), value = false)
    exec(testProgram, ntimes = fewRounds)(net)
    assertNetworkValues((0 to 8).zip(List(
      true, false, false,
      false, false, false,
      false, false, false
    )).toMap)(net)

    //after some time has passed id0 should return false
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
    //    net.valueMap[(String, Int)]().forall(e => e._2._1 == "hello" && e._2._2 > 0)
    net.valueMap[(Int, String)]().forall {
      case (_, (n, "hello")) if n > 0 => true
      case _ => false
    }

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
      override def main(): (Int, String) = evaporation(1000000, halving,"hello")
    }

    exec(testProgram, ntimes = fewRounds)(net)
    //net.valueMap[(String, Int)]().forall(e => e._2._1 == "hello" && e._2._2 > 0)
    net.valueMap[(Int, String)]().forall {
      case (_, (n, "hello")) if n > 0 => true
      case _ => false
    }

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
