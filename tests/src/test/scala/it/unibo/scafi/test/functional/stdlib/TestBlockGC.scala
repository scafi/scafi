package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.scafi.test.functional.ScafiTestUtils.{exec, manhattanNet, someRounds}
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec

class TestBlockGC extends AnyFunSpec with BeforeAndAfterEach {
  import ScafiTestUtils._

  private[this] class SimulationContextFixture(seeds: Seeds) {
    val net = manhattanNet(detachedNodesCoords = Set((2,2)), seeds = seeds)
  }

  val defaultNtimes: Int = someRounds

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockC with BlocksWithGC

  for(s <- seeds) {
    val seeds = Seeds(s, s, s)
    describe(s"BlockGC for $seeds") {
      it should behave like behaviours(seeds)
    }
  }

  def behaviours(seeds: Seeds): Unit = {
    describe("should support summarize"){
      it("basic scenario - everyone is a sink") {
        new SimulationContextFixture(seeds) {
          exec(new TestProgram {
            override def main(): Double = summarize(sink = true, _ + _, 1, 0)
          }, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 1, 1,
            1, 1, 1
          )).toMap)(net)
        }
      }
      it("basic scenario - no one is a sink") {
        new SimulationContextFixture(seeds) {
          exec(new TestProgram {
            override def main(): Double = summarize(sink = false, _ + _, 1, 0)
          }, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 1, 1,
            1, 1, 1
          )).toMap)(net)
        }
      }
      it("sink 4"){
        new SimulationContextFixture(seeds) {
          exec(new TestProgram {
            override def main(): Double = summarize(mid() == 4, _ + _, 1, 0)
          }, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            8, 8, 8,
            8, 8, 8,
            8, 8, 1
          )).toMap)(net)
        }
      }
      it("sink 0 and 4"){
        new SimulationContextFixture(seeds) {
          exec(new TestProgram {
            override def main(): Double = summarize(mid() == 4 || mid() == 0, _ + _, 1, 0)
          }, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            3, 3, 5,
            3, 5, 5,
            5, 5, 1
          )).toMap)(net)
        }
      }
      it("sink 0 and 6"){
        new SimulationContextFixture(seeds) {
          exec(new TestProgram {
            override def main(): Double = summarize(mid() == 6 || mid() == 0, _ + _, 1, 0)
          }, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            6, 6, 6,
            6, 6, 6,
            2, 2, 1
          )).toMap)(net)
        }
      }
    }
    describe("should support average") {
      it("average constant") {
        new SimulationContextFixture(seeds) {
          exec(new TestProgram {
            override def main(): Double = average(mid() == 0, 7)
          }, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            7, 7, 7,
            7, 7, 7,
            7, 7, 7
          )).toMap)(net)
        }
      }
      it("average id") {
        new SimulationContextFixture(seeds) {
          exec(new TestProgram {
            override def main(): Double = average(mid() == 0, mid())
          }, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            3.5, 3.5, 3.5,
            3.5, 3.5, 3.5,
            3.5, 3.5, 8
          )).toMap)(net)
        }
      }
    }
  }


}
