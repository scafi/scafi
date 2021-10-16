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

class TestSimplePrograms extends AnyFlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  private[this] class SimulationContextFixture(seeds: Seeds) {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, 1, 1), rng = 1.5, seeds = seeds))
    implicit val node = new BasicAggregateInterpreter
  }

  private[this] def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(name = "sensor1", value = 0)
    n.chgSensorValue(name = "sensor1", ids = Set(1,8), value = 1)
    n.addSensor(name = "sensor2", value = "off")
    n.chgSensorValue(name = "sensor2", ids = Set(2,8), value = "on")
    n
  }

  for(s <- seeds) {
    val seeds = Seeds(s, s, s)
    behavior of s"Simple programs for $seeds"
    it should behave like behaviours(seeds)
  }

  def behaviours(seeds: Seeds): Unit = {
    it should "evaluate a field of node ids" in new SimulationContextFixture(seeds) {
      import node._

      implicit val (endNet, _) = runProgram{ mid() } (net)

      assertNetworkValues((0 to 8).zip(0 to 8).toMap)
    }

    it should "work with sensors" in new SimulationContextFixture(seeds) {
      import node._

      implicit val (endNet, _) = runProgram{
        Tuple2(sense[Int]("sensor1"),sense[String]("sensor2"))
      } (net)

      assertNetworkValues((0 to 8).zip(List(
        (0,"off"),(1,"off"),(0,"on"),
        (0,"off"),(0,"off"),(0,"off"),
        (0,"off"),(0,"off"),(1,"on"))).toMap)
    }
  }
}
