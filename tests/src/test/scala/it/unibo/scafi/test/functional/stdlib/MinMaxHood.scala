/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._

class MinMaxHood extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

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

  def SetupNetwork(n: Network with SimulatorOps) = {
    n
  }

  Stdlib should "support min/maxHoodSelectors" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.minHoodSelector(nbr(mid))(nbr(mid)),
        includingSelf.minHoodSelector(nbr(mid))(nbr(mid)),
        excludingSelf.maxHoodSelector(nbr(mid))(nbr(mid)),
        includingSelf.maxHoodSelector(nbr(mid))(nbr(mid))
      )
    }, ntimes = fewRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (Some(1),0,Some(4),4), (Some(0),0,Some(5),5), (Some(1),1,Some(5),5),
      (Some(0),0,Some(7),7), (Some(0),0,Some(7),7), (Some(1),1,Some(7),7),
      (Some(3),3,Some(7),7), (Some(3),3,Some(6),7), (None,8,None,8)
    )).toMap)(net)

    // Assert this does not throw exception
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.minHoodSelector(Double.PositiveInfinity)(1),
        includingSelf.minHoodSelector(Int.MaxValue)(1),
        excludingSelf.maxHoodSelector(Double.NegativeInfinity)(1),
        includingSelf.maxHoodSelector(Int.MinValue)(1)
      )
    }, ntimes = fewRounds)(net)
  }
}
