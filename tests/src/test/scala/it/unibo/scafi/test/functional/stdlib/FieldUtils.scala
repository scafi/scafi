package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._


class FieldUtils extends FlatSpec {
  import ScafiAssertions._
  import ScafiTestUtils._

  val Field_Utils = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.0
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy,
        mapPos = (a,b,px,py) => if(a==2 && b==2) (100,100) else (px,py)), rng = 1.5))
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockG

  def SetupNetwork(n: Network with SimulatorOps): FunctionalTestIncarnation.Network with FunctionalTestIncarnation.SimulatorOps = {
    n
  }

  Field_Utils should "support min/maxHoodSelectors" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.minHoodSelector(nbr(mid))(nbr(mid)),
        includingSelf.minHoodSelector(nbr(mid))(nbr(mid)),
        excludingSelf.maxHoodSelector(nbr(mid))(nbr(mid)),
        includingSelf.maxHoodSelector(nbr(mid))(nbr(mid))
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
        includingSelf.maxHoodSelector(Int.MinValue)(1)
      )
    }, ntimes = fewRounds)(net)
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
      * Since the last device is alone it has no neighborhood no fold operation is perfomend
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
      (net.neighbourhood(0).size, net.neighbourhood(0).size + 1),
      (net.neighbourhood(1).size, net.neighbourhood(1).size + 1),
      (net.neighbourhood(2).size, net.neighbourhood(2).size + 1),
      (net.neighbourhood(3).size, net.neighbourhood(3).size + 1),
      (net.neighbourhood(4).size, net.neighbourhood(4).size + 1),
      (net.neighbourhood(5).size, net.neighbourhood(5).size + 1),
      (net.neighbourhood(6).size, net.neighbourhood(6).size + 1),
      (net.neighbourhood(7).size, net.neighbourhood(7).size + 1),
      (net.neighbourhood(8).size, net.neighbourhood(8).size + 1)
    )).toMap)(net)
  }

  Field_Utils should "support unionHood" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = (
        excludingSelf.unionHood(mid()),
        includingSelf.unionHood(mid())
      )
    }, ntimes = fewRounds)(net)

    //ASSERT
    /*
      Actual network:
      (Set(0),Set(0))	(Set(1),Set(1))	(Set(2),Set(2))
      (Set(3),Set(3))	(Set(4),Set(4))	(Set(5),Set(5))
      (Set(6),Set(6))	(Set(7),Set(7))	(Set(),Set(8))
     */
    assertNetworkValues((0 to 8).zip(List(
      ((0 to 7).toSet, (0 to 7).toSet),
      ((0 to 7).toSet, (0 to 7).toSet),
      ((0 to 7).toSet, (0 to 7).toSet),
      ((0 to 7).toSet, (0 to 7).toSet),
      ((0 to 7).toSet, (0 to 7).toSet),
      ((0 to 7).toSet, (0 to 7).toSet),
      ((0 to 7).toSet, (0 to 7).toSet),
      ((0 to 7).toSet, (0 to 7).toSet),
      (Set(), Set(8))
    )).toMap)(net)
  }
}
