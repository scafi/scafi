package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils.exec
import org.scalatest._

class SumHood extends FlatSpec{
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
        excludingSelf.sumHood(1),
        includingSelf.sumHood(1)
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (net.neighbourhood(0).size, net.neighbourhood(0).size + 1),
      (net.neighbourhood(1).size, net.neighbourhood(1).size + 1),
      (net.neighbourhood(2).size, net.neighbourhood(2).size + 1),
      (net.neighbourhood(3).size, net.neighbourhood(3).size + 1),
      (net.neighbourhood(4).size, net.neighbourhood(4).size + 1),
      (net.neighbourhood(5).size, net.neighbourhood(5).size + 1),
      (net.neighbourhood(6).size, net.neighbourhood(6).size + 1),
      (net.neighbourhood(7).size, net.neighbourhood(7).size + 1),
      (net.neighbourhood(8).size, net.neighbourhood(8).size + 1)
    )).toMap)(net)
  }
}
