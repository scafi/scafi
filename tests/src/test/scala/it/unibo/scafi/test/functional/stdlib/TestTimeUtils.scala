package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import it.unibo.utils.StatisticsUtils.stdDev
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec

import scala.concurrent.duration._
import scala.language.postfixOps

/*
Still to test:
- cyclicFunction
- cyclicFunctionWithDecay
 */
class TestTimeUtils extends AnyFunSpec with BeforeAndAfterEach {
  import ScafiTestUtils._

  var net: Network with SimulatorOps = _
  override protected def beforeEach(): Unit = net =  manhattanNet(detachedNodesCoords = Set((2,2)))

  private[this] trait TestProgramStandard extends AggregateProgram with ScafiStandardLanguage
    with StandardSensors with ScafiStandardLibraries.BuildingBlocks
  private[this] trait TestProgramFC extends AggregateProgram with ScafiFCLanguage
    with StandardSensors with ScafiFCLibraries.BuildingBlocks

  type ProgramImplDependencies = ScafiBaseLanguage with BuildingBlocksInterface

  val unitaryDecay: Int => Int = _ - 1
  val halving: Int => Int = _ / 2

  describe("TimeUtils") {
    describe("should support timerLocalTime") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Long = timerLocalTime(1.second)
      }

      def doTest(program: AggregateProgram) {
        exec(program, ntimes = someRounds)(net)

        ScafiAssertions.assertNetworkValuesWithPredicate[Long]((id, v) => v > 0.0, "Check timer didn't hit zero yet")()(net)

        val deltaTimeSensor = new StandardTemporalSensorNames {}.LSNS_DELTA_TIME
        net.addSensor[FiniteDuration](deltaTimeSensor, 0.2.second)

        exec(program, ntimes = someRounds)(net)

        ScafiAssertions.assertNetworkValuesWithPredicate[Long]((id,v) => v == 0.0, "Check timer hit zero")()(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }
    describe("should support impulsesEvery") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Any = rep(0)(_ + (if (impulsesEvery(1 nanosecond)) 1 else 0) )
      }

      def doTest(program: AggregateProgram): Unit = {
        exec(program, ntimes = manyManyRounds)(net)
        assert(net.valueMap[Int]().forall(e => e._2 > 0))
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }
    describe("should support sharedTimer") {
      val maxStdDev: Int = 10

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Any = sharedTimer(1 seconds)
      }

      def doTest(program: AggregateProgram): Unit = {
        exec(program, ntimes = someRounds)(net)
        assert(stdDev(net.valueMap[FiniteDuration]().filterKeys(_ != 8).values.map(_.toMillis)) < maxStdDev)

        exec(program, ntimes = manyManyRounds)(net)
        assert(stdDev(net.valueMap[FiniteDuration]().filterKeys(_ != 8).values.map(_.toMillis)) < maxStdDev)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }
    describe("should support recentlyTrue") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Boolean =
          recentlyTrue(1.second, cond = sense[Boolean]("rtSense"))
      }

      def doTest(program: AggregateProgram): Unit = {
        val deltaTimeSensor = new StandardTemporalSensorNames {}.LSNS_DELTA_TIME
        net.addSensor[Boolean]("rtSense", false)
        net.addSensor[FiniteDuration](deltaTimeSensor, 1.second)

        runProgramInOrder((0 to 8).toSeq, program)(net)
        assertNetworkValues((0 to 8).zip(List(
          false, false, false,
          false, false, false,
          false, false, false
        )).toMap, None, "Assert everything false")(net)

        net.chgSensorValue("rtSense", Set(0), value = true)
        net.chgSensorValue(deltaTimeSensor, Set(0), 0.3.seconds)
        runProgramInOrder(Seq(0,0), program)(net)
        assertNetworkValues((0 to 8).zip(List(
          true, false, false,
          false, false, false,
          false, false, false
        )).toMap, None, "Assert true in ID=0")(net)

        net.chgSensorValue("rtSense", Set(0), value = false)
        runProgramInOrder(Seq(0,0,0), program)(net)
        assertNetworkValues((0 to 8).zip(List(
          true, false, false,
          false, false, false,
          false, false, false
        )).toMap, None, "Assert still true in ID=0")(net)

        runProgramInOrder(Seq(0), program)(net)
        assertNetworkValues((0 to 8).zip(List(
          false, false, false,
          false, false, false,
          false, false, false
        )).toMap, None, "Assert back to false in ID=0 (and in the rest of the network)")(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }
    describe("should support evaporation") {
      val ceiling: Int = someRounds

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): (Int, String) = evaporation(ceiling, "hello")
      }

      def doTest(program: AggregateProgram): Unit = {
        exec(program, ntimes = manyRounds)(net)
        assert(net.valueMap[(Int, String)]().forall {
          case (_, (n, "hello")) if n > 0 => true
          case _ => false
        })

        exec(program, ntimes = manyManyRounds * 3)(net)
        assertNetworkValues((0 to 8).zip(List(
          (0, "hello"), (0, "hello"), (0, "hello"),
          (0, "hello"), (0, "hello"), (0, "hello"),
          (0, "hello"), (0, "hello"), (0, "hello")
        )).toMap)(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }
    describe("should support evaporation - with custom decay") {
      val ceiling: Int = 1000000

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): (Int, String) = evaporation(ceiling, halving,"hello")
      }

      def doTest(program: AggregateProgram): Unit = {
        exec(program, ntimes = fewRounds)(net)
        assert(net.valueMap[(Int, String)]().forall {
          case (_, (n, "hello")) if n > 0 => true
          case _ => false
        })

        exec(program, ntimes = manyManyRounds)(net)
        net.valueMap[(Int, String)]().forall {
          case (_, (0, "hello")) => true
          case _ => false
        }

        assertNetworkValues((0 to 8).zip(List(
          (0, "hello"), (0, "hello"), (0, "hello"),
          (0, "hello"), (0, "hello"), (0, "hello"),
          (0, "hello"), (0, "hello"), (0, "hello")
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
