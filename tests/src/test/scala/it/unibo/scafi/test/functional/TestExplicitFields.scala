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

class TestExplicitFields extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val ExplicitFields = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.5
  val SRC = "source"
  val FLAG = "flag"
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy), rng = 1.6))
  }

  private[this] trait TestLib { self: AggregateProgram with ExplicitFields with StandardSensors =>
    import scala.math.Numeric._

    def gradient(source: Boolean): Field[Double] =
      rep(Double.MaxValue){
        d => mux(source) { 0.0 } {
          (fnbr(d) + fsns(nbrRange)).minHoodPlus
        }
      }
  }

  private[this] class GradientProgram extends AggregateProgram with ExplicitFields with StandardSensors with TestLib {
    override type MainResult = Double
    override def main() = gradient(sense[Boolean](SRC)) + 1
  }

  private[this] class PartitionProgram extends AggregateProgram with ExplicitFields with StandardSensors with TestLib {
    override type MainResult = Double
    override def main() = branch(sense[Boolean](FLAG)){
      fnbr(1.0).withoutSelf.fold(0.0)(_ + _)
    }{
      gradient(sense[Boolean](SRC))
    }
  }

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(SRC, false)
    n.chgSensorValue(SRC, ids = Set(8), value = true)
    n.addSensor(FLAG, false)
    n.chgSensorValue(FLAG, ids = Set(0,1,2,3,4), value = true)
    n
  }

  ExplicitFields should "support construction of gradients" in new SimulationContextFixture {
    // ACT
    exec(new GradientProgram, ntimes = fewRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      6,    5,    4,
      4.5,  3.5,  2.5,
      3,    2,    1
    )).toMap)(net)
  }

  ExplicitFields should "support partitioning" in new SimulationContextFixture {
    // ACT
    exec(new PartitionProgram, ntimes = fewRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      2,    3,    1,
      2,    2,  1.5,
      2,    1,    0
    )).toMap)(net)
  }
}
