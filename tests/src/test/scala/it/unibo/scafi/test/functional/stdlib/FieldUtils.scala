package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._


class FieldUtils extends FlatSpec {
  import ScafiAssertions._
  import ScafiTestUtils._

  val Field_Utils = new ItWord


  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps = standardNetwork()
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockG

  Field_Utils should "support min/maxHoodSelectors" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.minHoodSelector(nbr(mid()))(nbr(mid())),
        includingSelf.minHoodSelector(nbr(mid()))(nbr(mid())),
        excludingSelf.maxHoodSelector(nbr(mid()))(nbr(mid())),
        includingSelf.maxHoodSelector(nbr(mid()))(nbr(mid()))
      )
    }, ntimes = fewRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (Some(1),0,Some(4),4), (Some(0),0,Some(5),5), (Some(1),1,Some(5),5),
      (Some(0),0,Some(7),7), (Some(0),0,Some(7),7), (Some(1),1,Some(7),7),
      (Some(3),3,Some(7),7), (Some(3),3,Some(6),7), (None,8,None,8)
    )).toMap)(net)

  }

  Field_Utils should "support anyHood" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.anyHood(mid() == nbr(mid())),
        includingSelf.anyHood(mid() == nbr(mid()))
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (false, true), (false, true), (false, true),
      (false, true), (false, true), (false, true),
      (false, true), (false, true), (false, true)
    )).toMap)(net)
  }

  Field_Utils should "support anyHood, one neighbour with true value" in new SimulationContextFixture {
    // ACT
    net.addSensor[Boolean](name = "sensorZero", value = false)
    net.chgSensorValue("sensorZero", Set(0), true)
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.anyHood(nbr(sense[Boolean]("sensorZero"))),
        includingSelf.anyHood(nbr(sense[Boolean]("sensorZero")))
      )
    }, ntimes = fewRounds)(net)
    net.chgSensorValue("sensorZero", Set(0), false)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (false, true), (true, true), (false, false),
      (true, true), (true, true), (false, false),
      (false, false), (false, false), (false, false)
    )).toMap)(net)
  }

  Field_Utils should "support anyHood, more neighbours with true value" in new SimulationContextFixture {
    // ACT
    net.addSensor[Boolean](name = "sensorZero", value = false)
    net.chgSensorValue("sensorZero", Set(0,3), true)
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.anyHood(nbr(sense[Boolean]("sensorZero"))),
        includingSelf.anyHood(nbr(sense[Boolean]("sensorZero")))
      )
    }, ntimes = fewRounds)(net)
    net.chgSensorValue("sensorZero", Set(0, 3), false)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (true, true), (true, true), (false, false),
      (true, true), (true, true), (false, false),
      (true, true), (true, true), (false, false)
    )).toMap)(net)
  }
  
  Field_Utils should "support everyHood" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.everyHood(mid() == nbr(mid())),
        includingSelf.everyHood(mid() == nbr(mid()))
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    /*
      * def everyHood(expr: => Boolean): Boolean = foldhoodTemplate(true)(_&&_)(expr)
      * everyHood is initialized to true.
      * Since the last device is alone it has no neighborhood no fold operation is performed
      *   resulting in a final true vale
      */
    assertNetworkValues((0 to 8).zip(List(
      (false, false), (false, false), (false, false),
      (false, false), (false, false), (false, false),
      (false, false), (false, false), (true, true)
    )).toMap)(net)
  }

  Field_Utils should "support sumHood" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.sumHood(1),
        includingSelf.sumHood(1)
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (net.neighbourhood(0).size, net.neighbourhood(0).size + 1), (net.neighbourhood(1).size, net.neighbourhood(1).size + 1), (net.neighbourhood(2).size, net.neighbourhood(2).size + 1),
      (net.neighbourhood(3).size, net.neighbourhood(3).size + 1), (net.neighbourhood(4).size, net.neighbourhood(4).size + 1), (net.neighbourhood(5).size, net.neighbourhood(5).size + 1),
      (net.neighbourhood(6).size, net.neighbourhood(6).size + 1), (net.neighbourhood(7).size, net.neighbourhood(7).size + 1), (net.neighbourhood(8).size, net.neighbourhood(8).size + 1)
    )).toMap)(net)
  }

  Field_Utils should "support sumHood - mid" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.sumHood(nbr(mid())),
        includingSelf.sumHood(nbr(mid()))
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (net.neighbourhood(0).sum, net.neighbourhood(0).sum), (net.neighbourhood(1).sum, net.neighbourhood(1).sum + 1), (net.neighbourhood(2).sum, net.neighbourhood(2).sum + 2),
      (net.neighbourhood(3).sum, net.neighbourhood(3).sum + 3), (net.neighbourhood(4).sum, net.neighbourhood(4).sum + 4), (net.neighbourhood(5).sum, net.neighbourhood(5).sum + 5),
      (net.neighbourhood(6).sum, net.neighbourhood(6).sum + 6), (net.neighbourhood(7).sum, net.neighbourhood(7).sum + 7), (net.neighbourhood(8).sum, net.neighbourhood(8).sum + 8)
    )).toMap)(net)
  }

  Field_Utils should "support unionHood" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.unionHood(nbr(mid())),
        includingSelf.unionHood(nbr(mid()))
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (net.neighbourhood(0), net.neighbourhood(0) + 0), (net.neighbourhood(1), net.neighbourhood(1) + 1), (net.neighbourhood(2), net.neighbourhood(2) + 2),
      (net.neighbourhood(3), net.neighbourhood(3) + 3), (net.neighbourhood(4), net.neighbourhood(4) + 4), (net.neighbourhood(5), net.neighbourhood(5) + 5),
      (net.neighbourhood(6), net.neighbourhood(6) + 6), (net.neighbourhood(7), net.neighbourhood(7) + 7), (net.neighbourhood(8), net.neighbourhood(8) + 8)
    )).toMap)(net)
  }

  Field_Utils should "support mergeHoodFirst" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.mergeHoodFirst(nbr(Map(mid() -> mid()))),
        includingSelf.mergeHoodFirst(nbr(Map(mid() -> mid())))
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (net.neighbourhood(0).map(e => (e,e)).toMap, (net.neighbourhood(0) + 0).map(e => (e,e)).toMap), (net.neighbourhood(1).map(e => (e,e)).toMap, (net.neighbourhood(1) + 1).map(e => (e,e)).toMap), (net.neighbourhood(2).map(e => (e,e)).toMap, (net.neighbourhood(2) + 2).map(e => (e,e)).toMap),
      (net.neighbourhood(3).map(e => (e,e)).toMap, (net.neighbourhood(3) + 3).map(e => (e,e)).toMap), (net.neighbourhood(4).map(e => (e,e)).toMap, (net.neighbourhood(4) + 4).map(e => (e,e)).toMap), (net.neighbourhood(5).map(e => (e,e)).toMap, (net.neighbourhood(5) + 5).map(e => (e,e)).toMap),
      (net.neighbourhood(6).map(e => (e,e)).toMap, (net.neighbourhood(6) + 6).map(e => (e,e)).toMap), (net.neighbourhood(7).map(e => (e,e)).toMap, (net.neighbourhood(7) + 7).map(e => (e,e)).toMap), (net.neighbourhood(8).map(e => (e,e)).toMap, (net.neighbourhood(8) + 8).map(e => (e,e)).toMap)
    )).toMap)(net)
  }

  Field_Utils should "support minHoodLoc" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.minHoodLoc(Int.MaxValue)(nbr(mid())),
        includingSelf.minHoodLoc(Int.MaxValue)(nbr(mid()))
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    assertNetworkValues((0 to 8).zip(List(
      (1, 0), (0, 0), (1, 1),
      (0, 0), (0,0), (1, 1),
      (3, 3), (3, 3), (Int.MaxValue, 8)
    )).toMap)(net)
  }
}
