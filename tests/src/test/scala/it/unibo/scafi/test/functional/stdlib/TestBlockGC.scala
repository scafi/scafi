package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils.{exec, someRounds, standardNetwork}
import org.scalatest._

class TestBlockGC extends FunSpec with BeforeAndAfterEach {

  val net: Network with SimulatorOps = standardNetwork()

  val defaultNtimes: Int = someRounds

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockC with BlocksWithGC

  describe("BlockGC") {
    describe("should support summarize"){
      it("basic scenario - everyone is a sink") {
        exec(new TestProgram {
          override def main(): Double = summarize(sink = true, _ + _, 1, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 1, 1,
          1, 1, 1
        )).toMap)(net)
      }
      it("basic scenario - no one is a sink") {
        exec(new TestProgram {
          override def main(): Double = summarize(sink = false, _ + _, 1, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 1, 1,
          1, 1, 1
        )).toMap)(net)
      }
      it("sink 4"){
        exec(new TestProgram {
          override def main(): Double = summarize(mid() == 4, _ + _, 1, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          8, 8, 8,
          8, 8, 8,
          8, 8, 1
        )).toMap)(net)
      }
      it("sink 0 and 4"){
        exec(new TestProgram {
          override def main(): Double = summarize(mid() == 4 || mid() == 0, _ + _, 1, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          3, 3, 5,
          3, 5, 5,
          5, 5, 1
        )).toMap)(net)
      }
      it("sink 0 and 6"){
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


}
