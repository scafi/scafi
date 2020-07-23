package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils.exec
import org.scalatest._

class EveryHood extends FlatSpec{
  val Stdlib = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.0
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy,
        mapPos = (a,b,px,py) => if(a==2 && b==2) (100,100) else (px,py)), rng = 1.5))
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with FieldUtils

  def SetupNetwork(n: Network with SimulatorOps): FunctionalTestIncarnation.Network with FunctionalTestIncarnation.SimulatorOps = {
    n
  }

  Stdlib should "support everyHood" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.everyHood(mid() == nbr(mid())),
        includingSelf.everyHood(mid() == nbr(mid()))
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    /*
      * def everyHood(expr: => Boolean): Boolean = foldhoodTemplate(true)(_&&_)(expr)
      * everyHood is initialized to true.
      * Since the last device is alone it has no neighborhood no fold operation is perfomend
      *   resulting in a final true vale
      */
    assertNetworkValues((0 to 8).zip(List(
      (false, false), (false, false), (false, false),
      (false, false), (false, false), (false, false),
      (false, false), (false, false), (true, true)
    )).toMap)(net)
  }
}
