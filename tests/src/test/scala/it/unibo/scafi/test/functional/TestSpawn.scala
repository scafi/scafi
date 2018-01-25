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

class TestSpawn extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  private val SpawnConstruct, Processes, ManyProcesses = new ItWord

  val stepx = 1.0
  val stepy = 1.0

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy), rng = 1.2))
    implicit val program = new Program
  }

  private[this] class Program extends AggregateProgram with Spawn with FieldUtils with StandardSensors with BlockG {
    override type MainResult = Any

    def src = sense[Boolean]("src")
    def gen1 = sense[Boolean]("gen1")
    def gen2 = sense[Boolean]("gen2")

    override def main(): Any = {
      var procs = Map(
        1 -> SpawnDef(1, ()=>f"${distanceTo(gen1)}%.1f", genCondition = () => gen1),
        2 -> SpawnDef(2, ()=>f"${distanceTo(src)}%.1f", genCondition = () => gen2, limit = 2.5))

      var keyGen = procs.values.filter(_.genCondition()).map(_.pid)

      procs.map { case (pid,proc) => pid -> spawn(proc) }
    }
  }

  type ProcsMap = Map[Int,Option[String]]

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor("gen1", false)
    n.addSensor("gen2", false)
    n.addSensor("src", false)
    n.chgSensorValue("src", Set(8), true)
    n
  }

  Processes must "not exist if not activated" in new SimulationContextFixture {
    // ACT
    exec(program)(net)

    // ASSERT
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2==None) }(net)
  }

  Processes must "exist when activated" in new SimulationContextFixture {
    // ACT
    exec(program, ntimes = 100)(net)
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2==None) }(net)

    net.chgSensorValue("gen1", Set(8), true)

    exec(program, ntimes = 500)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      Map(1 -> S("4.0"), 2-> None), Map(1 -> S("3.0"), 2-> None), Map(1 -> S("2.0"), 2-> None),
      Map(1 -> S("3.0"), 2-> None), Map(1 -> S("2.0"), 2-> None), Map(1 -> S("1.0"), 2-> None),
      Map(1 -> S("2.0"), 2-> None), Map(1 -> S("1.0"), 2-> None), Map(1 -> S("0.0"), 2-> None)
    )).toMap)(net)
  }

  Processes can "have a limited extension" in new SimulationContextFixture {
    // ARRANGE
    net.chgSensorValue("gen2", Set(0), true)

    // ACT
    exec(program, ntimes = 500)(net)

    // ASSERT
    assertForAllNodes[ProcsMap]{ (id, m) => m.forall(proc => proc match {
      case (1, value) => value == None
      case (2, None) => Set(5,7,8).contains(id)
      case (2, Some(value)) => value.toDouble > 10
    })}(net)

    // ACT
    net.chgSensorValue("src", Set(4), true)
    exec(program, ntimes = 500)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      Map(1 -> None, 2 -> S("2.0")), Map(1 -> None, 2 -> S("1.0")), Map(1 -> None, 2 -> S("2.0")),
      Map(1 -> None, 2 -> S("1.0")), Map(1 -> None, 2 -> S("0.0")), Map(1 -> None, 2-> None),
      Map(1 -> None, 2 -> S("2.0")), Map(1 -> None, 2->      None), Map(1 -> None, 2-> None)
    )).toMap)(net)
  }

  Processes can "be extinguished when stopped being generated" in new SimulationContextFixture {
    // ARRANGE
    net.chgSensorValue("gen1", Set(0), true)

    // ACT (process activation)
    exec(program, ntimes = 500)(net)

    // ASSERT
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall{ case (pid,value) => if(pid==1) value.isDefined else value.isEmpty} }(net)

    // ACT (process deactivation and garbage collection)
    net.chgSensorValue("gen1", Set(0), false)
    exec(program, ntimes = 2000)(net)

    // ASSERT
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2.isEmpty) }(net)
  }

  ManyProcesses must "coexist without interference" in new SimulationContextFixture {
    // ARRANGE
    net.chgSensorValue("gen1", Set(0), true)
    net.chgSensorValue("gen2", Set(6), true)

    // ACT
    exec(program, ntimes = 500)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      Map(1 -> S("0.0"), 2 -> S("4.0")), Map(1 -> S("1.0"), 2 ->     None), Map(1 -> S("2.0"), 2 -> None),
      Map(1 -> S("1.0"), 2 -> S("3.0")), Map(1 -> S("2.0"), 2 -> S("2.0")), Map(1 -> S("3.0"), 2 -> None),
      Map(1 -> S("2.0"), 2 -> S("2.0")), Map(1 -> S("3.0"), 2 -> S("1.0")), Map(1 -> S("4.0"), 2 -> S("0.0"))
    )).toMap)(net)
  }

  private def S[T](x: T) = Some[T](x)
}
