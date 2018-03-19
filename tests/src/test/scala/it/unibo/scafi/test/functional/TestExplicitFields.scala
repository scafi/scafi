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
import scala.collection.{Map=>M}

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

    def gradient(source: Boolean): Double =
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

  private[this] class PartitionProgram extends AggregateProgram with ExplicitFields with StandardSensors {
    override type MainResult = Double
    override def main() = branch(sense[Boolean](FLAG)){
      fnbr(1.0).withoutSelf
    }{
      fnbr(-1.0)
    }.fold(0.0)(_ + _)
  }

  private[this] class DomainMismatchProgram extends AggregateProgram with ExplicitFields with StandardSensors {
    override type MainResult = Double
    override def main(): Double = {
      val f1 = branch(sense[Boolean](FLAG)){ fnbr(1.0).withoutSelf }{ fnbr(-1.0) }
      val f2 = branch(sense[Boolean](SRC)){ fnbr(10.0) }{ fnbr(0.0) }
      (f1.map2(f2)(_ + _)).fold(0.0)(_ + _)
    }
  }

  private[this] class DomainWithDefaultsProgram extends AggregateProgram with ExplicitFields with StandardSensors {
    override type MainResult = Map[ID,String]
    override def main(): Map[ID,String] = {
      val f1: Field[String] = branch(sense[Boolean](FLAG)){ fnbr("a").withoutSelf }{ fnbr("b") }
      val f2: Field[String] = branch(!sense[Boolean](SRC)){ fnbr("c") }{ fnbr("d") }
      (f1.map2d(f2)("x")(_ + _)).toMap
    }
  }

  private[this] class DomainUnionProgram extends AggregateProgram with ExplicitFields with StandardSensors {
    override type MainResult = Map[ID,String]
    override def main(): Map[ID,String] = {
      val f1: Field[String] = branch(sense[Boolean](FLAG)){ fnbr("a").withoutSelf }{ fnbr("b") }
      val f2: Field[String] = branch(!sense[Boolean](SRC)){ fnbr("c") }{ fnbr("d") }
      (f1.map2u(f2)("x","w")(_ + _)).toMap
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
      2,    2,   -2,
      -2,   -3,   -3
    )).toMap)(net)
  }

  ExplicitFields should "deal with domain mismatches" in new SimulationContextFixture {
    an [Exception] should be thrownBy exec(new DomainMismatchProgram, ntimes = fewRounds)(net)
  }

  ExplicitFields should "support defaults to deal with domain mismatches" in new SimulationContextFixture {
    // ACT
    exec(new DomainWithDefaultsProgram, ntimes = fewRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      M(1->"ac", 3->"ac"), M(0->"ac", 2->"ac", 4->"ac"), M(1->"ac"),
      M(4->"ac", 0->"ac"), M(1->"ac", 3->"ac"),          M(5->"bc", 8->"bx"),
      M(6->"bc", 7->"bc"), M(6->"bc", 7->"bc", 8->"bx"), M(5->"bx", 7->"bx", 8->"bd")
    )).toMap)(net)
  }

  ExplicitFields should "support defaults on both sides to deal with domain mismatches" in new SimulationContextFixture {
    // ACT
    exec(new DomainUnionProgram, ntimes = fewRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      M(0->"xc",1->"ac",3->"ac"),         M(0->"ac",1->"xc",2->"ac",4->"ac"),         M(1->"ac",2->"xc",5->"xc"),
      M(0->"ac",3->"xc",4->"ac",6->"xc"), M(1->"ac",3->"ac",4->"xc",5->"xc",7->"xc"), M(2->"xc",4->"xc",5->"bc",8->"bw"),
      M(3->"xc",6->"bc",7->"bc"),         M(4->"xc",6->"bc",7->"bc",8->"bw"),         M(5->"bw",7->"bw",8->"bd")
    )).toMap)(net)
  }
}
