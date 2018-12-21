/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._

class TestStandardLibrary extends FlatSpec with Matchers {
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
        includingSelf.maxHoodSelector(nbr(mid))(nbr(mid)),
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
        includingSelf.maxHoodSelector(Int.MinValue)(1),
      )
    }, ntimes = fewRounds)(net)
  }
}
