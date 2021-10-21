package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import it.unibo.utils.StatisticsUtils.stdDev
import org.scalatest._

import scala.concurrent.duration._
import org.scalatest.flatspec.AnyFlatSpec

/*
Still to test:
- cyclicFunction
- cyclicFunctionWithDecay
 */
class TestTimeUtils extends AnyFlatSpec{
  import ScafiTestUtils._

  val Time_Utils = new ItWord

  private[this] class SimulationContextFixture(seeds: Seeds) {
    val net: Network with SimulatorOps = manhattanNet(detachedNodesCoords = Set((2,2)), seeds = seeds)
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BuildingBlocks

  val unitaryDecay: Int => Int = _ - 1
  val halving: Int => Int = _ / 2

  for(s <- seeds) {
    val seeds = Seeds(s, s, s)
    behavior of s"TimeUtils for $seeds"
    it should behave like behaviours(seeds)
  }

  def behaviours(seeds: Seeds): Unit = {
    Time_Utils should "support timerLocalTime" in new SimulationContextFixture(seeds) {
      val testProgram: TestProgram = new TestProgram {
        override def main(): Long = timerLocalTime(1.second)
      }

      exec(testProgram, ntimes = someRounds)(net)

      ScafiAssertions.assertNetworkValuesWithPredicate[Long]((id, v) => v > 0.0, "Check timer didn't hit zero yet")()(net)

      val deltaTimeSensor = new StandardTemporalSensorNames {}.LSNS_DELTA_TIME
      net.addSensor[FiniteDuration](deltaTimeSensor, 0.2.second)

      exec(testProgram, ntimes = someRounds)(net)

      ScafiAssertions.assertNetworkValuesWithPredicate[Long]((id,v) => v == 0.0, "Check timer hit zero")()(net)
    }

    Time_Utils should "support impulsesEvery" in new SimulationContextFixture(seeds) {
      exec(new TestProgram {
        override def main(): Any = rep(0)(_ + (if (impulsesEvery(1.nanosecond)) 1 else 0) )
      }, ntimes = manyManyRounds)(net)

      assert(net.valueMap[Int]().forall(e => e._2 > 0))
    }

    Time_Utils should "support sharedTimer" in new SimulationContextFixture(seeds) {
      val maxStdDev: Int = 10

      val testProgram: TestProgram = new TestProgram {
        override def main(): Any = sharedTimer(1.seconds)
      }

      exec(testProgram, ntimes = someRounds)(net)
      assert(stdDev(net.valueMap[FiniteDuration]().filterKeys(_ != 8).values.map(_.toMillis)) < maxStdDev)

      exec(testProgram, ntimes = manyManyRounds)(net)
      assert(stdDev(net.valueMap[FiniteDuration]().filterKeys(_ != 8).values.map(_.toMillis)) < maxStdDev)
    }

    Time_Utils should "support recentlyTrue" in new SimulationContextFixture(seeds) {
      val deltaTimeSensor = new StandardTemporalSensorNames {}.LSNS_DELTA_TIME
      net.addSensor[Boolean]("rtSense", false)
      net.addSensor[FiniteDuration](deltaTimeSensor, 1.second)

      val testProgram: TestProgram = new TestProgram {
        override def main(): Boolean =
          recentlyTrue(1.second, cond = sense[Boolean]("rtSense"))
      }

      runProgramInOrder((0 to 8).toSeq, testProgram)(net)
      assertNetworkValues((0 to 8).zip(List(
        false, false, false,
        false, false, false,
        false, false, false
      )).toMap, None, "Assert everything false")(net)

      net.chgSensorValue("rtSense", Set(0), value = true)
      net.chgSensorValue(deltaTimeSensor, Set(0), 0.3.seconds)
      runProgramInOrder(Seq(0,0), testProgram)(net)
      assertNetworkValues((0 to 8).zip(List(
        true, false, false,
        false, false, false,
        false, false, false
      )).toMap, None, "Assert true in ID=0")(net)

      net.chgSensorValue("rtSense", Set(0), value = false)
      runProgramInOrder(Seq(0,0,0), testProgram)(net)
      assertNetworkValues((0 to 8).zip(List(
        true, false, false,
        false, false, false,
        false, false, false
      )).toMap, None, "Assert still true in ID=0")(net)

      runProgramInOrder(Seq(0), testProgram)(net)
      assertNetworkValues((0 to 8).zip(List(
        false, false, false,
        false, false, false,
        false, false, false
      )).toMap, None, "Assert back to false in ID=0 (and in the rest of the network)")(net)
    }

    Time_Utils should("support evaporation") in new SimulationContextFixture(seeds) {
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

    Time_Utils should("support evaporation - with custom decay") in new SimulationContextFixture(seeds) {
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
}
