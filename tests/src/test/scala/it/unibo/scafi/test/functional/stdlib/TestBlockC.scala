package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec

class TestBlockC extends AnyFunSpec with BeforeAndAfterEach {
  import ScafiTestUtils._

  var net: Network with SimulatorOps = _
  def restartNetwork(): Unit = {
    net = ScafiTestUtils.manhattanNet(detachedNodesCoords = Set((2,2)))
    net.addSensor(name = "source", value = false)
  }
  override protected def beforeEach(): Unit = restartNetwork()

  val defaultNtimes: Int = someRounds

  private[this] trait TestProgramDefs {
    def hopGradient(source: Boolean): Double
  }

  private[this] trait TestProgramStandard extends AggregateProgram with ScafiStandardLanguage
    with StandardSensors with ScafiStandardLibraries.BlockC with TestProgramDefs {

    override def hopGradient(source: Boolean): Double =
      rep(Double.PositiveInfinity) { hops => {mux(source) {0.0} {1 + minHood(nbr {hops})}}}
  }

  private[this] trait TestProgramFC extends AggregateProgram with ScafiFCLanguage
    with StandardSensors with ScafiFCLibraries.BlockC with TestProgramDefs {

    override def hopGradient(source: Boolean): Double =
      rep(Double.PositiveInfinity) { hops => {mux(source) {0.0} {1 + nbrField{hops}.minHood}}}
  }

  private[this] type ProgramImplDependencies = BlockCInterface with ScafiBaseLanguage with TestProgramDefs

  describe(s"BlockC") {
    describe("should support findParent") {
      describe("should work with a constant value") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): ID = findParent(1)
        }
        def doTest(program: AggregateProgram) {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            Int.MaxValue, Int.MaxValue, Int.MaxValue,
            Int.MaxValue, Int.MaxValue, Int.MaxValue,
            Int.MaxValue, Int.MaxValue, Int.MaxValue
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      /*
        mid() induces a gradient field
        ( 0  1  2
          3  4  5
          6  7  8 )
      * */
      describe("should work with mid") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): ID = findParent(mid())
        }
        def doTest(program: AggregateProgram) {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            Int.MaxValue, 0, 1,
            0, 0, 1,
            3, 3, Int.MaxValue
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
    }
    describe("should support C") {
      def sum: (Int, Int) => Int = _ + _
      describe("should produce a constant filed if every node has the same potential") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): ID = C(1, sum, Int.MaxValue, 0)
        }
        def doTest(program: AggregateProgram) {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            Int.MaxValue, Int.MaxValue, Int.MaxValue,
            Int.MaxValue, Int.MaxValue, Int.MaxValue,
            Int.MaxValue, Int.MaxValue, Int.MaxValue
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("should accumulate on the device with the lowest potential") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): ID = C(mid(), sum, 1, 0)
        }
        def doTest(program: AggregateProgram) {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            8, 3, 1,
            3, 1, 1,
            1, 1, 1
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("should accumulate along a gradient - one source") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): ID = C(hopGradient(sense("source")), sum, 1, 0)
        }
        def doTest(program: AggregateProgram) {
          net.addSensor[Boolean]("source", false)
          net.chgSensorValue("source", Set(4), true)
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 8, 1,
            1, 1, 1
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("should accumulate along a gradient - two sources") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): ID = C(hopGradient(sense("source")), sum, 1, 0)
        }
        def doTest(program: AggregateProgram) {
          net.addSensor[Boolean]("source", false)
          net.chgSensorValue("source", Set(0,7), true)
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            5, 2, 1,
            1, 1, 1,
            1, 3, 1
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
    }
  }
}
