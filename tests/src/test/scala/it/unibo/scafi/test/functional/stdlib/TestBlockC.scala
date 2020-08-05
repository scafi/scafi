package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.utils.StatisticsUtils._
import org.scalatest._

class TestBlockC extends FunSpec {
  import ScafiTestUtils._

  val net: Network with SimulatorOps = standardNetwork()

  val defaultNtimes: Int = someRounds

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockC

  describe("BlockC") {
    describe("should support smaller") {
      it ("should work in basic scenario - all true") {
        exec(new TestProgram {
          override def main(): Boolean = smaller(1, 2)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          true, true, true,
          true, true, true,
          true, true, true
        )).toMap)(net)
      }
      it ("should work in basic scenario - all false") {
        exec(new TestProgram {
          override def main(): Boolean = smaller(2, 1)
        }, ntimes = defaultNtimes)(net)

        assertNetworkValues((0 to 8).zip(List(
          false, false, false,
          false, false, false,
          false, false, false
        )).toMap)(net)
      }
      it ("should work with nbr operations") {
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
  }

}
