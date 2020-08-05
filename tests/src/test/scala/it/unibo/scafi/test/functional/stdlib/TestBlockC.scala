package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import org.scalatest._

class TestBlockC extends FunSpec with BeforeAndAfterEach {
  import ScafiTestUtils._

  var net: Network with SimulatorOps = standardNetwork()
  def restartNetwork(): Unit = {
    net = standardNetwork()
    net.addSensor(name = "source", value = false)
  }
  override protected def beforeEach(): Unit = restartNetwork()

  val defaultNtimes: Int = someRounds

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockC {
    def hopGradient(source: Boolean): Double =
      rep(Double.PositiveInfinity) { hops => {mux(source) {0.0} {1 + minHood(nbr {hops})}}}
  }


  describe("BlockC") {
    describe("should support smaller") {
      it("should work in basic scenario - all true") {
        exec(new TestProgram {
          override def main(): Boolean = smaller(1, 2)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          true, true, true,
          true, true, true,
          true, true, true
        )).toMap)(net)
      }
      it("should work in basic scenario - all false") {
        exec(new TestProgram {
          override def main(): Boolean = smaller(2, 1)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          false, false, false,
          false, false, false,
          false, false, false
        )).toMap)(net)
      }
      it("should work with nbr operations") {
        exec(new TestProgram {
          override def main(): Boolean = smaller(mid(), minHoodPlus(nbr(mid())))
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          true, false, false,
          false, false, false,
          false, false, true
        )).toMap)(net)
      }
    }
    describe("should support findParent") {
      it("should work with a constant value") {
        exec(new TestProgram {
          override def main(): ID = findParent(1)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          Int.MaxValue, Int.MaxValue, Int.MaxValue,
          Int.MaxValue, Int.MaxValue, Int.MaxValue,
          Int.MaxValue, Int.MaxValue, Int.MaxValue
        )).toMap)(net)
      }
      it("should work with mid") {
        exec(new TestProgram {
          override def main(): ID = findParent(mid())
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          Int.MaxValue, 0, 1,
          0, 0, 1,
          3, 3, Int.MaxValue
        )).toMap)(net)
      }
    }
    describe("should support C") {
      def sum: (Int, Int) => Int = _ + _

      it("should produce a constant filed if every has the same potential") {
        exec(new TestProgram {
          override def main(): ID = C(1, sum, Int.MaxValue, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          Int.MaxValue, Int.MaxValue, Int.MaxValue,
          Int.MaxValue, Int.MaxValue, Int.MaxValue,
          Int.MaxValue, Int.MaxValue, Int.MaxValue
        )).toMap)(net)
      }
      it("should accumulate on the device with the lowest potential") {
        exec(new TestProgram {
          override def main(): ID = C(mid(), sum, 1, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          8, 3, 1,
          3, 1, 1,
          1, 1, 1
        )).toMap)(net)
      }
      it("should accumulate along a gradient - one source") {
        net.addSensor[Boolean]("source", false)
        net.chgSensorValue("source", Set(4), true)
        exec(new TestProgram {
          override def main(): ID = C(hopGradient(sense("source")), sum, 1, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 8, 1,
          1, 1, 1
        )).toMap)(net)
      }
      it("should accumulate along a gradient - two sources") {
        net.addSensor[Boolean]("source", false)
        net.chgSensorValue("source", Set(0,7), true)
        exec(new TestProgram {
          override def main(): ID = C(hopGradient(sense("source")), sum, 1, 0)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          5, 2, 1,
          1, 1, 1,
          1, 3, 1
        )).toMap)(net)
      }

    }
  }
}
