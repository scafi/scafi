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

class TestChannel extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val Channel = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.0
  val SRC = "source"
  val FLAG = "flag"
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps = newNet
    def newNet = SetupNetwork(simulatorFactory.gridLike(GridSettings(4, 4, stepx, stepy), rng = 1.1))
  }

  val SRC_ID = 15
  val DEST_ID = 0
  val OBSTACLES = Set(5,6,9)

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with FieldUtils with BlockG {
    def src = sense[Boolean]("src")
    def dest = sense[Boolean]("dest")
    def obstacle = sense[Boolean]("obstacle")
  }

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor[Boolean]("obstacle", false)
    n.chgSensorValue("obstacle", OBSTACLES, true)
    n.addSensor[Boolean]("src", false)
    n.chgSensorValue("src", Set(SRC_ID), true)
    n.addSensor[Boolean]("dest", false)
    n.chgSensorValue("dest", Set(DEST_ID), true)
    n
  }

  Channel should "be false everywhere when no source or destination is available" in new SimulationContextFixture {
    assertAlways(new TestProgram {
      override def main(): Any = channel(false, true, 2)
    }, ntimes = someRounds) {
      (id:Int,v:Any) => v==false
    }(newNet)

    assertAlways(new TestProgram {
      override def main(): Any = (distanceTo(false), distanceBetween(src, false))
    }, ntimes = someRounds){
      (id:Int,v:Any) => v==(Double.PositiveInfinity,Double.PositiveInfinity)
    }(newNet)

    assertAlways(new TestProgram {
      override def main(): Any = channel(src, false, 0.5)
    }, ntimes = someRounds){
      (id:Int,v:Any) => v==false || id==SRC_ID
    }(newNet)
  }

  Channel should "compute a path, if it exists" in new SimulationContextFixture {
    val (n1,n2,n3) = (newNet, newNet, newNet)

    // NOTE: for device 10:  distTo(str) + distTo(dest) < distBetween(src,dest) + w
    //                       2           + 6            < 6                     + w
    // So to be included, it needs to be:  w > 2

    exec(new TestProgram {
      override def main(): Any = branch(obstacle){ false }{ channel(src, dest, 2+0.1) }
    }, ntimes = manyRounds)(n1)

    // ASSERT
    assertNetworkValues((0 to 15).zip(List(
      true,  true,  true,  true,
      true, false, false, true,
      true, false,  true, true,
      true,  true,  true, true,
    )).toMap)(n1)

    exec(new TestProgram {
      override def main(): Any = branch(obstacle){ false }{ channel(src, dest, 2-0.1) }
    }, ntimes = manyRounds)(n2)

    // ASSERT
    assertNetworkValues((0 to 15).zip(List(
      true,  true,  true,  true,
      true, false, false, true,
      true, false, false, true,
      true,  true,  true, true,
    )).toMap)(n2)

    exec(new TestProgram {
      override def main(): Any = branch(obstacle){ false }{ channel(src, dest, 0) }
    }, ntimes = manyRounds)(n3)

    // ASSERT
    assertNetworkValues((0 to 15).zip(List(
      true,  true,  true,  true,
      true, false, false, true,
      true, false, false, true,
      true,  true,  true, true,
    )).toMap)(n3)
  }
}
