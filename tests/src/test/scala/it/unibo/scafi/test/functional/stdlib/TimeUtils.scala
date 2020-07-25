package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.utils.StatisticsUtils.stdDev
import org.scalatest._

import scala.concurrent.duration._


class TimeUtils extends FlatSpec{
  import ScafiTestUtils._

  val Time_Utils = new ItWord
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
}
