package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import org.scalatest._

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



}
