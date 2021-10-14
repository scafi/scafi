/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestNewProcesses extends AnyFlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  private val SpawnConstruct, Processes, ManyProcesses = new ItWord

  // Sensor names for de/activating processes, and for gradient sources
  val Gen1 = "gen1"
  val Gen2 = "gen2"
  val SRC  = "src"
  val STOP = "stop"
  // Network constants
  val (stepx, stepy) = (1.0, 1.0)
  // Simulation constants
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 25000)

  import SpawnInterface._

  private[this] trait SimulationContextFixture {
    implicit val net: NetworkSimulator =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy), rng = 1.2)).asInstanceOf[NetworkSimulator]
    val largeNet: NetworkSimulator =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(20, 20, stepx, stepy), rng = 1.2)).asInstanceOf[NetworkSimulator]
    val program = new Program
  }

  case class Pid(dev: ID, k: Long)

  private[this] class Program extends AggregateProgram
    with CustomSpawn with FieldUtils with StandardSensors with BlockG with StateManagement {
    def src = sense[Boolean](SRC)
    def stop = sense[Boolean](STOP)
    def gen1 = sense[Boolean](Gen1)
    def gen2 = sense[Boolean](Gen2)

    override def main(): Any = {
      val k = countChanges(gen1, gen1 || false)
      val procs1 = sspawn[Pid,Unit,String](pid => args => {
        (""+distanceTo(gen1), if(stop) Terminated else Output)
      }, if(k._2) Set(Pid(mid,k._1)) else Set.empty, ())

      val j = countChanges(gen2, gen2 || false)
      val procs2 = sspawn[Pid,Boolean,String](pid => args => {
        val maxExpansion = distanceTo(pid.dev==mid)
        val g = distanceTo(args)
        (""+g, if(maxExpansion>2.5) External else Output)
      }, if(j._2) Set(Pid(mid,j._1)) else Set.empty, src)

      (procs1 ++ procs2)
    }
  }

  type ProcsMap = Map[Pid,String]

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(Gen1, false)
    n.addSensor(Gen2, false)
    n.addSensor(SRC, false)
    n.addSensor(STOP, false)
    n.chgSensorValue(SRC, Set(8), true)
    n
  }

  import NetworkDsl._

  Processes must "not exist if not activated" in new SimulationContextFixture {
    // ACT: run program comprising spawns
    exec(program, ntimes = fewRounds)(net)

    // ASSERT: nobody computed a value for any process, i.e., nobody executed any process
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2==None) }(net)
  }

  Processes must "exist when activated" in new SimulationContextFixture {
    // ACT: run program, checking nobody run any process
    exec(program, ntimes = fewRounds)(net)
    assertForAllNodes[ProcsMap]{ (_,m) => m.forall(_._2==None) }(net)

    // ACT: activate process 1 from node 8, run program
    setSensor(Gen1, true).inDevices(8)
    exec(program, ntimes = someRounds)(net)

    val p1 = Pid(8,1)
    // ASSERT: process 1 has been executed in the network; a correct gradient has stabilised
    assertNetworkValues((0 to 8).zip(List(
      Map(p1 -> "4.0"), Map(p1 -> "3.0"), Map(p1 -> "2.0"),
      Map(p1 -> "3.0"), Map(p1 -> "2.0"), Map(p1 -> "1.0"),
      Map(p1 -> "2.0"), Map(p1 -> "1.0"), Map(p1 -> "0.0")
    )).toMap)(net)
  }

  Processes can "have a limited extension" in new SimulationContextFixture {
    // ARRANGE: activate process 2 from node 0, which is configured to have a limited extension
    net.chgSensorValue(Gen2, Set(0), true)

    // ACT: run program
    exec(program, ntimes = someRounds)(net)

    // ASSERT: check the limited extension of the process, which doesn't reach to gradient source;
    //         check nodes not covered; the other nodes compute a rising gradient (without source)
    val p021 = Pid(0,1)
    assertForAllNodes[ProcsMap]{ (id, m) => m.forall(proc => proc match {
      case (p021, value) => Set(0,1,2,3,4,6).contains(id) && value.toDouble > 10
      case _ => Set(5,7,8).contains(id)
    })}(net)

    // ACT: set new source; continue program execution
    setSensor(SRC, true).inDevices(4)
    exec(program, ntimes = someRounds)(net)

    // ASSERT: check gradient stabilises in the covered area
    assertNetworkValues((0 to 8).zip(List(
      Map(p021 -> "2.0"), Map(p021 -> "1.0"), Map(p021 -> "2.0"),
      Map(p021 -> "1.0"), Map(p021 -> "0.0"), Map(             ),
      Map(p021 -> "2.0"), Map(             ), Map(             )
    )).toMap)(net)
  }

  Processes can "be extinguished when stopped being generated" in new SimulationContextFixture {
    implicit val network = largeNet

    // ARRANGE: activate process 1 from node 0
    setSensor(Gen1, true).inDevices(0)

    // ACT (process activation)
    exec(program, ntimes = manyManyRounds)(largeNet)

    // ASSERT
    val p011 = Pid(0,1)
    assertForAllNodes[ProcsMap]{ (_,m) => m.contains(p011) && m.size==1}(largeNet)

    // ACT (process deactivation and garbage collection)
    setSensor(STOP, true).inDevices(0)
    exec(program, ntimes = manyManyRounds)(largeNet)

    // ASSERT
    assertForAllNodes[ProcsMap]{ (_,m) => m.isEmpty }(largeNet)
  }

  ManyProcesses must "coexist without interference" in new SimulationContextFixture {
    // ARRANGE: generate process 1 from node 0 and process 2 from node 6
    setSensor(Gen1, true).inDevices(0)
    setSensor(Gen2, true).inDevices(6)

    // ACT: run program
    exec(program, ntimes = someRounds)(net)

    // ASSERT: check both processes running gradients get globally evaluated without interference
    val p1 = Pid(0,1)
    val p2 = Pid(6,1)
    assertNetworkValues((0 to 8).zip(List(
      Map(p1 -> "0.0", p2 -> "4.0"), Map(p1 -> "1.0"             ), Map(p1 -> "2.0"             ),
      Map(p1 -> "1.0", p2 -> "3.0"), Map(p1 -> "2.0", p2 -> "2.0"), Map(p1 -> "3.0"             ),
      Map(p1 -> "2.0", p2 -> "2.0"), Map(p1 -> "3.0", p2 -> "1.0"), Map(p1 -> "4.0", p2 -> "0.0")
    )).toMap)(net)
  }

  Processes should "not conflict when generated from different nodes" in new SimulationContextFixture {
    // BUT NOTE: sensor gen1 also represents the source for the gradient of process 1
    // ARRANGE: generate process 1 from both node 0 and node 8
    setSensor(Gen1, true).inDevices(0)
    setSensor(Gen1, true).inDevices(8)

    // ACT: run program
    exec(program, ntimes = someRounds)(net)

    // ASSERT: result from running process 1
    val p1 = Pid(0,1)
    val p2 = Pid(8,1)
    assertNetworkValues((0 to 8).zip(List(
      Map(p1 -> "0.0", p2 -> "0.0"), Map(p1 -> "1.0", p2 -> "1.0"), Map(p1 -> "2.0", p2 -> "2.0"),
      Map(p1 -> "1.0", p2 -> "1.0"), Map(p1 -> "2.0", p2 -> "2.0"), Map(p1 -> "1.0", p2 -> "1.0"),
      Map(p1 -> "2.0", p2 -> "2.0"), Map(p1 -> "1.0", p2 -> "1.0"), Map(p1 -> "0.0", p2 -> "0.0")
    )).toMap)(net)

    // ACT: add an additional generator, and continue program execution
    setSensor(Gen1, true).inDevices(4)
    exec(program, ntimes = someRounds)(net)

    // ASSERT: check no conflict when process 1 is generated from multiple nodes possibly activated at different times
    val p3 = Pid(4,1)
    assertNetworkValues((0 to 8).zip(List(
      Map(p1 -> "0.0", p2 -> "0.0", p3 -> "0.0"), Map(p1 -> "1.0", p2 -> "1.0", p3 -> "1.0"), Map(p1 -> "2.0", p2 -> "2.0", p3 -> "2.0"),
      Map(p1 -> "1.0", p2 -> "1.0", p3 -> "1.0"), Map(p1 -> "0.0", p2 -> "0.0", p3 -> "0.0"), Map(p1 -> "1.0", p2 -> "1.0", p3 -> "1.0"),
      Map(p1 -> "2.0", p2 -> "2.0", p3 -> "2.0"), Map(p1 -> "1.0", p2 -> "1.0", p3 -> "1.0"), Map(p1 -> "0.0", p2 -> "0.0", p3 -> "0.0")
    )).toMap)(net)
  }

  Processes should "not conflict when generated from the same node" in new SimulationContextFixture {
    // ARRANGE+ACT: generate process 1 twice from node 0
    setSensor(Gen2, true).inDevices(7)
    exec(program, ntimes = someRounds)(net)
    setSensor(Gen2, false).inDevices(7)
    exec(program, ntimes = someRounds)(net)

    // ASSERT: result from running process 1
    val p1 = Pid(7,1)
    val p2 = Pid(7,2)
    assertNetworkValues((0 to 8).zip(List(
      Map(                        ), Map(p1 -> "3.0", p2 -> "3.0"), Map(                        ),
      Map(p1 -> "3.0", p2 -> "3.0"), Map(p1 -> "2.0", p2 -> "2.0"), Map(p1 -> "1.0", p2 -> "1.0"),
      Map(p1 -> "2.0", p2 -> "2.0"), Map(p1 -> "1.0", p2 -> "1.0"), Map(p1 -> "0.0", p2 -> "0.0")
    )).toMap)(net)
  }


  Processes should "be resilient to partitions: reconnecting node" in new SimulationContextFixture {
    // ARRANGE: generate process 1 from node 0
    setSensor(Gen1, true).inDevices(0)

    // ACT: run program
    exec(program, ntimes = someRounds)(net)

    // ACT: detach node (keeping it alive), turn off generator, and run program (for all except detached node)
    val nodeNbrhoodToRestore = detachNode(8, net) // detach node
    setSensor(STOP, true).inDevices(0) // stop generating process 1
    execProgramFor(program, ntimes = manyRounds)(net)(id => id != 8) // execute except for detached device

    // ASSERT: process 1 has disappeared everywhere except in detached node
    val p1 = Pid(0,1)
    assertForAllNodes[ProcsMap]{
      case (8,m) => m.contains(p1) && m.size==1
      case (_,m) => m.isEmpty
    }(net)

    // ACT: reconnect node and run program globally
    connectNode(8, nodeNbrhoodToRestore, net)
    exec(program, ntimes = manyRounds)(net)

    // ASSERT: eventually, the process has disappeared
    assertForAllNodes[ProcsMap]{ (_,m) => m.isEmpty }(net)
  }

  private def S[T](x: T) = Some[T](x)
}
