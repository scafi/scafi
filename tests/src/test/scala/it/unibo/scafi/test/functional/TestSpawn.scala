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

  // Process identifiers
  val P1 = 1
  val P2 = 2
  // Sensor names for de/activating processes, and for gradient sources
  val Gen1 = "gen1"
  val Gen2 = "gen2"
  val SRC  = "src"
  // Network constants
  val (stepx, stepy) = (1.0, 1.0)
  // Simulation constants
  val FewRounds      = 100
  val SomeRounds     = 500
  val ManyRounds     = 1000
  val ManyManyRounds = 2000

  private[this] trait SimulationContextFixture {
    implicit val net: NetworkSimulator =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy), rng = 1.2)).asInstanceOf[NetworkSimulator]
    val program = new Program
  }

  private[this] class Program extends AggregateProgram with Spawn with FieldUtils with StandardSensors with BlockG {
    override val TimeGC: Long = 20

    override type MainResult = Any

    def src = sense[Boolean](SRC)
    def gen1 = sense[Boolean](Gen1)
    def gen2 = sense[Boolean](Gen2)

    override def main(): Any = {
      var procs = Map(
        P1 -> SpawnDef(P1, ()=>f"${distanceTo(gen1)}%.1f", genCondition = () => gen1),
        P2 -> SpawnDef(P2, ()=>f"${distanceTo(src)}%.1f", genCondition = () => gen2, limit = 2.5))

      procs.map { case (pid,proc) => pid -> spawn(proc).value }
    }
  }

  type ProcsMap = Map[Int,Option[String]]

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(Gen1, false)
    n.addSensor(Gen2, false)
    n.addSensor(SRC, false)
    n.chgSensorValue(SRC, Set(8), true)
    n
  }

  import NetworkDsl._

  Processes must "not exist if not activated" in new SimulationContextFixture {
    // ACT: run program comprising spawns
    exec(program)(net)

    // ASSERT: nobody computed a value for any process, i.e., nobody executed any process
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2==None) }(net)
  }

  Processes must "exist when activated" in new SimulationContextFixture {
    // ACT: run program, checking nobody run any process
    exec(program, ntimes = FewRounds)(net)
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2==None) }(net)

    // ACT: activate process 1 from node 8, run program
    setSensor(Gen1, true).inDevices(8)
    exec(program, ntimes = SomeRounds)(net)

    // ASSERT: process 1 has been executed in the network; a correct gradient has stabilised
    assertNetworkValues((0 to 8).zip(List(
      Map(P1 -> S("4.0"), P2 -> None), Map(P1 -> S("3.0"), P2 -> None), Map(P1 -> S("2.0"), P2 -> None),
      Map(P1 -> S("3.0"), P2 -> None), Map(P1 -> S("2.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None),
      Map(P1 -> S("2.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None), Map(P1 -> S("0.0"), P2 -> None)
    )).toMap)(net)
  }

  Processes can "have a limited extension" in new SimulationContextFixture {
    // ARRANGE: activate process 2 from node 0, which is configured to have a limited extension
    net.chgSensorValue(Gen2, Set(0), true)

    // ACT: run program
    exec(program, ntimes = SomeRounds)(net)

    // ASSERT: check the limited extension of the process, which doesn't reach to gradient source;
    //         check nodes not covered; the other nodes compute a rising gradient (without source)
    assertForAllNodes[ProcsMap]{ (id, m) => m.forall(proc => proc match {
      case (1, value) => value == None
      case (2, None) => Set(5,7,8).contains(id)
      case (2, Some(value)) => value.toDouble > 10
    })}(net)

    // ACT: set new source; continue program execution
    setSensor(SRC, true).inDevices(4)
    exec(program, ntimes = SomeRounds)(net)

    // ASSERT: check gradient stabilises in the covered area
    assertNetworkValues((0 to 8).zip(List(
      Map(P1 -> None, P2 -> S("2.0")), Map(P1 -> None, P2 -> S("1.0")), Map(P1 -> None, P2 -> S("2.0")),
      Map(P1 -> None, P2 -> S("1.0")), Map(P1 -> None, P2 -> S("0.0")), Map(P1 -> None, P2 ->     None),
      Map(P1 -> None, P2 -> S("2.0")), Map(P1 -> None, P2 ->     None), Map(P1 -> None, P2 ->     None)
    )).toMap)(net)
  }

  Processes can "be extinguished when stopped being generated" in new SimulationContextFixture {
    // ARRANGE: activate process 1 from node 0
    setSensor(Gen1, true).inDevices(0)

    // ACT (process activation)
    exec(program, ntimes = SomeRounds)(net)

    // ASSERT
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall{ case (pid,value) => if(pid==1) value.isDefined else value.isEmpty} }(net)

    // ACT (process deactivation and garbage collection)
    setSensor(Gen1, false).inDevices(0)
    exec(program, ntimes = ManyManyRounds)(net)

    // ASSERT
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2.isEmpty) }(net)
  }

  ManyProcesses must "coexist without interference" in new SimulationContextFixture {
    // ARRANGE: generate process 1 from node 0 and process 2 from node 6
    setSensor(Gen1, true).inDevices(0)
    setSensor(Gen2, true).inDevices(6)

    // ACT: run program
    exec(program, ntimes = SomeRounds)(net)

    // ASSERT: check both processes running gradients get globally evaluated without interference
    assertNetworkValues((0 to 8).zip(List(
      Map(P1 -> S("0.0"), P2 -> S("4.0")), Map(P1 -> S("1.0"), P2 ->     None), Map(P1 -> S("2.0"), P2 ->     None),
      Map(P1 -> S("1.0"), P2 -> S("3.0")), Map(P1 -> S("2.0"), P2 -> S("2.0")), Map(P1 -> S("3.0"), P2 ->     None),
      Map(P1 -> S("2.0"), P2 -> S("2.0")), Map(P1 -> S("3.0"), P2 -> S("1.0")), Map(P1 -> S("4.0"), P2 -> S("0.0"))
    )).toMap)(net)
  }

  Processes should "not conflict when generated from different nodes" in new SimulationContextFixture {
    // BUT NOTE: sensor gen1 also represents the source for the gradient of process 1
    // ARRANGE: generate process 1 from both node 0 and node 8
    setSensor(Gen1, true).inDevices(0)
    setSensor(Gen1, true).inDevices(8)

    // ACT: run program
    exec(program, ntimes = SomeRounds)(net)

    // ASSERT: result from running process 1
    assertNetworkValues((0 to 8).zip(List(
      Map(P1 -> S("0.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None), Map(P1 -> S("2.0"), P2 -> None),
      Map(P1 -> S("1.0"), P2 -> None), Map(P1 -> S("2.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None),
      Map(P1 -> S("2.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None), Map(P1 -> S("0.0"), P2 -> None)
    )).toMap)(net)

    // ACT: add an additional generator, and continue program execution
    setSensor(Gen1, true).inDevices(4)
    exec(program, ntimes = SomeRounds)(net)

    // ASSERT: check no conflict when process 1 is generated from multiple nodes possibly activated at different times
    assertNetworkValues((0 to 8).zip(List(
      Map(P1 -> S("0.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None), Map(P1 -> S("2.0"), P2 -> None),
      Map(P1 -> S("1.0"), P2 -> None), Map(P1 -> S("0.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None),
      Map(P1 -> S("2.0"), P2 -> None), Map(P1 -> S("1.0"), P2 -> None), Map(P1 -> S("0.0"), P2 -> None)
    )).toMap)(net)
  }

  Processes should "be resilient to partitions: reconnecting node" in new SimulationContextFixture {
    // ARRANGE: generate process 1 from node 0
    setSensor(Gen1, true).inDevices(0)

    // ACT: run program
    exec(program, ntimes = FewRounds)(net)

    // ACT: detach node (keeping it alive), turn off generator, and run program (for all except detached node)
    val nodeNbrhoodToRestore = detachNode(8, net) // detach node
    setSensor(Gen1, false).inDevices(0) // stop generating process 1
    execProgramFor(program, ntimes = SomeRounds)(net)(id => id != 8) // execute except for detached device

    // ASSERT: process 1 has disappeared everywhere except in detached node
    assertForAllNodes[ProcsMap]{
      case (8,m) => m(1).isDefined && m(2).isEmpty
      case (id,m) => m.forall(_._2.isEmpty)
    }(net)

    // ACT: reconnect node and run program globally
    connectNode(8, nodeNbrhoodToRestore, net)
    exec(program, ntimes = ManyRounds)(net)

    // ASSERT: eventually, the process has disappeared
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2==None) }(net)
  }

  private def S[T](x: T) = Some[T](x)
}
