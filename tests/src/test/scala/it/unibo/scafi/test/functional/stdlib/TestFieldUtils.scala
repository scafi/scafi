package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._


class TestFieldUtils extends FlatSpec {
  import ScafiAssertions._
  import ScafiTestUtils._

  val Field_Utils = new ItWord

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps = manhattanNet(detachedNodesCoords = Set((2,2)))
    val n: Int => Set[Int] = net.neighbourhood
  }

  private[this] trait TestProgram extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils


  Field_Utils should "support min/maxHoodSelectors" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.minHoodSelector(nbr(mid()))(nbr(mid())),
        includingSelf.minHoodSelector(nbr(mid()))(nbr(mid())),
        excludingSelf.maxHoodSelector(nbr(mid()))(nbr(mid())),
        includingSelf.maxHoodSelector(nbr(mid()))(nbr(mid()))
      )
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (Some(1),0,Some(4),4), (Some(0),0,Some(5),5), (Some(1),1,Some(5),5),
      (Some(0),0,Some(7),7), (Some(0),0,Some(7),7), (Some(1),1,Some(7),7),
      (Some(3),3,Some(7),7), (Some(3),3,Some(6),7), (None,8,None,8)
    )).toMap)(net)

  }

  Field_Utils should "support anyHood" in new SimulationContextFixture {
    val privateNet: Network with SimulatorOps = manhattanNet(detachedNodesCoords = Set((0,2), (2,2)))
    privateNet.addSensor[Boolean]("sensor", false)
    privateNet.chgSensorValue("sensor", Set(0, 1, 8), true)

    exec(new TestProgram {
      override def main(): (Boolean, Boolean) = (
        includingSelf.anyHood(nbr{sense[Boolean]("sensor")}),
        excludingSelf.anyHood(nbr{sense[Boolean]("sensor")}))
    }, ntimes = someRounds)(privateNet)

    assert(privateNet.neighbourhood(2) == Set(1,4,5))

    /*
    * 2 -> has only one neighbour with true
    * 3 -> has 2+ neighbours with true
    * 6 -> isolated device with false
    * 7 -> every neighbour is false
    * 8 -> isolated device with true
    */
    assertNetworkValues((0 to 8).zip(List(
      (true, true), (true, true), (true, true),
      (true, true), (true, true), (true, true),
      (false, false), (false, false), (true, false)
    )).toMap)(privateNet)

  }

  Field_Utils should "support everyHood" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): (Boolean, Boolean) = (
        excludingSelf.everyHood(mid() == nbr(mid())),
        includingSelf.everyHood(mid() == nbr(mid()))
      )
    }, ntimes = someRounds)(net)

    /*
     * Since the last device has no neighborhood no fold operation is performed.
     * This results in its final value to be true.
     */
    assertNetworkValues((0 to 8).zip(List(
      (false, false), (false, false), (false, false),
      (false, false), (false, false), (false, false),
      (false, false), (false, false), (true, true)
    )).toMap)(net)
  }

  Field_Utils should "support sumHood" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): (Int, Int) = (
        excludingSelf.sumHood(1),
        includingSelf.sumHood(1)
      )
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (n(0).size, n(0).size + 1), (n(1).size, n(1).size + 1), (n(2).size, n(2).size + 1),
      (n(3).size, n(3).size + 1), (n(4).size, n(4).size + 1), (n(5).size, n(5).size + 1),
      (n(6).size, n(6).size + 1), (n(7).size, n(7).size + 1), (n(8).size, n(8).size + 1)
    )).toMap)(net)
  }

  Field_Utils should "support sumHood - simplified" in new SimulationContextFixture {
    net.addSensor[Int]("snsSumHood", 0)
    net.chgSensorValue("snsSumHood", Set(0,1,2), 10)

    exec(new TestProgram {
      override def main(): (Int, Int) = (
        excludingSelf.sumHood(nbr{sense[Int]("snsSumHood")}),
        includingSelf.sumHood(nbr{sense[Int]("snsSumHood")})
      )
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (10,20), (20,30), (10,20),
      (20,20), (30,30), (20,20),
      (0,0), (0,0), (0,0)
    )).toMap)(net)

    net.addSensor[Int]("snsSumHood", 0)
    net.chgSensorValue("snsSumHood", Set(0,1,2), 15)
    net.chgSensorValue("snsSumHood", Set(4), -20)

    exec(new TestProgram {
      override def main(): (Int, Int) = (
        excludingSelf.sumHood(nbr{sense[Int]("snsSumHood")}),
        includingSelf.sumHood(nbr{sense[Int]("snsSumHood")})
      )
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (-5,10), (10,25), (-5,10),
      (10,10), (45,25), (10,10),
      (-20,-20), (-20,-20), (0,0)
    )).toMap)(net)
  }

  Field_Utils should "support unionHood" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main():(Set[ID], Set[ID]) = (
        excludingSelf.unionHood(nbr(mid())),
        includingSelf.unionHood(nbr(mid()))
      )
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (n(0), n(0) + 0), (n(1), n(1) + 1), (n(2), n(2) + 2),
      (n(3), n(3) + 3), (n(4), n(4) + 4), (n(5), n(5) + 5),
      (n(6), n(6) + 6), (n(7), n(7) + 7), (n(8), n(8) + 8)
    )).toMap)(net)
  }

  Field_Utils should "support mergeHoodFirst" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main():(Map[ID, ID], Map[ID, ID]) = (
        excludingSelf.mergeHoodFirst(nbr(Map(mid() -> mid()))),
        includingSelf.mergeHoodFirst(nbr(Map(mid() -> mid())))
      )
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (n(0).map(e => (e,e)).toMap, (n(0) + 0).map(e => (e,e)).toMap), (n(1).map(e => (e,e)).toMap, (n(1) + 1).map(e => (e,e)).toMap), (n(2).map(e => (e,e)).toMap, (n(2) + 2).map(e => (e,e)).toMap),
      (n(3).map(e => (e,e)).toMap, (n(3) + 3).map(e => (e,e)).toMap), (n(4).map(e => (e,e)).toMap, (n(4) + 4).map(e => (e,e)).toMap), (n(5).map(e => (e,e)).toMap, (n(5) + 5).map(e => (e,e)).toMap),
      (n(6).map(e => (e,e)).toMap, (n(6) + 6).map(e => (e,e)).toMap), (n(7).map(e => (e,e)).toMap, (n(7) + 7).map(e => (e,e)).toMap), (n(8).map(e => (e,e)).toMap, (n(8) + 8).map(e => (e,e)).toMap)
    )).toMap)(net)
  }

  Field_Utils should "support mergeHoodFirst - merge" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Map[Int, ID] = includingSelf.mergeHoodFirst(nbr(Map(1 -> mid())))
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      Map(1 -> 0), Map(1 -> 1), Map(1 -> 2),
      Map(1 -> 3), Map(1 -> 4), Map(1 -> 5),
      Map(1 -> 6), Map(1 -> 7), Map(1 -> 8)
    )).toMap)(net)

    exec(new TestProgram {
      override def main(): Map[Int, ID] = excludingSelf.mergeHoodFirst(nbr(Map(1 -> mid())))
    }, ntimes = someRounds)(net)

    assert(net.valueMap[Map[Int, ID]]().forall {
      case (id, map) if id == 8 => map.isEmpty
      case (id, map) => map.size == 1 && map.forall(p => n(id).contains(p._2))
    })
  }

  Field_Utils should "support mergeHood - merge" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Map[Int, ID] = excludingSelf.mergeHood(nbr(Map(1 -> mid())))((_, y) => y)
    }, ntimes = someRounds)(net)

    assert(net.valueMap[Map[Int, ID]]().forall {
      case (id, map) if id == 8 => map.isEmpty
      case (id, map) => map.size == 1 && map.forall(p => n(id).contains(p._2))
    })
  }

  Field_Utils should "support mergeHood - merge including" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Map[Int, ID] = includingSelf.mergeHood(nbr(Map(1 -> mid())))((_, y) => y)
    }, ntimes = someRounds)(net)

    assert(net.valueMap[Map[Int, ID]]().forall {
      case (id, map) if id == 8 => map.size == 1 && map(1) == 8
      case (id, map) => map.size == 1 && map.forall(p => n(id).contains(p._2))
    })
  }

  Field_Utils should "support minHoodLoc" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): (Int, Int) = (
        excludingSelf.minHoodLoc(Int.MaxValue)(nbr(mid())),
        includingSelf.minHoodLoc(Int.MaxValue)(nbr(mid()))
      )
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      (1, 0), (0, 0), (1, 1),
      (0, 0), (0,0), (1, 1),
      (3, 3), (3, 3), (Int.MaxValue, 8)
    )).toMap)(net)
  }
}
