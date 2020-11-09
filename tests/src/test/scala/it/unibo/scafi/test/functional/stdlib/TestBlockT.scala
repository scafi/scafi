package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils
import it.unibo.utils.StatisticsUtils._
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec


/*
Not yet tested:
- cyclicTimerWithDecay
- clock
 */
class TestBlockT extends AnyFunSpec with BeforeAndAfterEach {
  import ScafiTestUtils._

  val Block_T = new ItWord

  var net: Network with SimulatorOps = _

  def restartNetwork(): Unit = net = ScafiTestUtils.manhattanNet(detachedNodesCoords = Set((2,2)))

  override protected def beforeEach(): Unit = restartNetwork()

  private[this] trait TestProgramStandard extends AggregateProgram with ScafiStandardLanguage
    with StandardSensors with ScafiStandardLibraries.BlockT

  private[this] trait TestProgramFC extends AggregateProgram with ScafiFCLanguage
    with StandardSensors with ScafiFCLibraries.BlockT

  type ProgramImplDependencies = BlockTInterface with ScafiBaseLanguage

  def unitaryDecay: Int => Int = _ - 1
  def halving: Int => Int = _ / 2

  describe("Block T") {
    describe("should support T with unitary decay and 0 floor value") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>

        override def main(): Int = T(10, 0, unitaryDecay)
      }

      def doTest(program: AggregateProgram) {
        exec(program, ntimes = someRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          0, 0, 0,
          0, 0, 0,
          0, 0, 0
        )).toMap)(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("should initialize as specified") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>

        override def main(): Int = T(10, 0, identity[Int])
      }

      def doTest(program: AggregateProgram) {
        exec(program, ntimes = fewRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          10, 10, 10,
          10, 10, 10,
          10, 10, 10
        )).toMap)(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("should be consistent in intermediate tests") {
      val ceiling: Int = someRounds

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>

        override def main(): (Int, Int) = (
          T(ceiling, 0, unitaryDecay),
          //this can be seen as a round counter
          rep(0)(_ + 1)
        )
      }

      def doTest(program: AggregateProgram) {
        exec(program, ntimes = someRounds)(net)
        assert(net.valueMap[(Int, Int)]().forall { case (_, (decayTimer: Int, roundsPerformed: Int)) => decayTimer + roundsPerformed <= ceiling + 1})

        for(_ <- 1 to 20) {
          exec(program, ntimes = fewRounds)(net)
          assert(net.valueMap[(Int, Int)]().forall { case (_, (decayTimer: Int, roundsPerformed: Int)) => decayTimer + roundsPerformed <= ceiling + 1})
        }

        exec(program, ntimes = manyManyRounds * 2)(net)
        assert(net.valueMap[(Int, Int)]().forall { case (_, (decayTimer: Int, roundsPerformed: Int)) => decayTimer + roundsPerformed >= ceiling + 1})
        assert(net.valueMap[(Int, Int)]().forall { case (_, (decayTimer: Int, roundsPerformed: Int)) => decayTimer == 0})
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("should support T with unitary decay and custom floor value") {
      val floorValue = 1

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Int = T(10, floorValue, unitaryDecay)
      }

      def doTest(program: AggregateProgram) = {
        exec(program, ntimes = someRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          floorValue, floorValue, floorValue,
          floorValue, floorValue, floorValue,
          floorValue, floorValue, floorValue
        )).toMap)(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("should support T with unitary decay and negative floor value") {
      val floorValue: Int = -10

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Int = T(10, floorValue, unitaryDecay)
      }

      def doTest(program: AggregateProgram) = {
        exec(program, ntimes = someRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          floorValue, floorValue, floorValue,
          floorValue, floorValue, floorValue,
          floorValue, floorValue, floorValue
        )).toMap)(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("should ") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Int = timer(10)
      }

      def doTest(program: AggregateProgram) = {
        exec(program, ntimes = someRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          0, 0, 0,
          0, 0, 0,
          0, 0, 0
        )).toMap)(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("should support limitedMemory") {
      val value: Int = 10
      val expValue: Int = -1

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): (Int, Int) = (limitedMemory(value, expValue, 10)._1, limitedMemory(value, expValue, manyManyRounds)._1)
      }

      def doTest(program: AggregateProgram) = {
        exec(program, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          (expValue, value), (expValue, value), (expValue, value),
          (expValue, value), (expValue, value), (expValue, value),
          (expValue, value), (expValue, value), (expValue, value)
        )).toMap)(net)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("should support sharedTimerWithDecay") {
      val maxStdDev: Int = 10

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Int = sharedTimerWithDecay(1, 1)
      }

      def doTest(program: AggregateProgram) = {
        exec(program, ntimes = manyManyRounds)(net)

        //standard deviation inside the same group should be low
        assert(stdDev(net.valueMap[Int]().filterKeys(_ != 8).values) < maxStdDev)
      }

      it(inStandard) {
        doTest(new TestProgramStandard with ProgramMain)
      }
      it(inFC) {
        doTest(new TestProgramFC with ProgramMain)
      }
    }

    describe("T should restart after branch switch") {
      val timeToEstinguish = 10

      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Int =
          branch(sense[Boolean]("snsT")) {
            if (T(timeToEstinguish, 0, unitaryDecay) == 0) { 10 } else { 20 }
          } {
            -1
          }
      }

      def doTest(program: AggregateProgram) = {
        net.addSensor[Boolean]("snsT", false)

        net.chgSensorValue("snsT", Set(0), true)
        val s1 = schedulingSequence((0 to 8).toSet, someRounds).ensureAtLeast(id=0, timeToEstinguish)
        runProgramInOrder(s1, program)(net)
        assertNetworkValues((0 to 8).zip(List(
          10, -1, -1,
          -1, -1, -1,
          -1, -1, -1
        )).toMap, None, s"Assert ID=0 (for which snsT=true) yields 10\n Scheduling: $s1")(net)

        net.chgSensorValue("snsT", Set(0), false)
        runProgramInOrder(schedulingSequence((0 to 8).toSet, someRounds), program)(net)
        assertNetworkValues((0 to 8).zip(List(
          -1, -1, -1,
          -1, -1, -1,
          -1, -1, -1
        )).toMap, None, "Assert ID=0 (for which snsT=false) yields -1")(net)

        import ScafiTestUtils.SchedulingSeq
        net.chgSensorValue("snsT", Set(0), true)
        runProgramInOrder(schedulingSequence((0 to 8).toSet[ID], someRounds).ensureLessOrEqualThan(id = 0, timeToEstinguish-1), program)(net)
        assertNetworkValues((0 to 8).zip(List(
          20, -1, -1,
          -1, -1, -1,
          -1, -1, -1
        )).toMap, None, "Assert ID=0 (for which snsT=true) reenters the branch and re-evaluates T")(net)
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
