package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.functional.ScafiTestUtils._
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec

class TestBlockGC extends AnyFunSpec with BeforeAndAfterEach {

  var net: Network with SimulatorOps = _

  override protected def beforeEach(): Unit = net = manhattanNet(detachedNodesCoords = Set((2,2)))

  val defaultNtimes: Int = someRounds

  private[this] trait TestProgramStandard extends AggregateProgram with ScafiStandardLanguage
    with StandardSensors with ScafiStandardLibraries.BlockC with ScafiStandardLibraries.BlocksWithGC
  private[this] trait TestProgramFC extends AggregateProgram with ScafiFCLanguage
    with StandardSensors with ScafiFCLibraries.BlockC with ScafiFCLibraries.BlocksWithGC

  type ProgramImplDependencies = ScafiBaseLanguage with BlocksWithGCInterface

  describe("BlockGC") {
    describe("should support summarize"){
      describe("basic scenario - everyone is a sink") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): Double = summarize(sink = true, _ + _, 1, 0)
        }

        def doTest(program: AggregateProgram): Unit = {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 1, 1,
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
      describe("basic scenario - no one is a sink") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): Double = summarize(sink = false, _ + _, 1, 0)
        }

        def doTest(program: AggregateProgram): Unit = {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 1, 1,
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
      describe("sink 4"){
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): Double = summarize(mid() == 4, _ + _, 1, 0)
        }

        def doTest(program: AggregateProgram): Unit = {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            8, 8, 8,
            8, 8, 8,
            8, 8, 1
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("sink 0 and 4"){
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): Double = summarize(mid() == 4 || mid() == 0, _ + _, 1, 0)
        }

        def doTest(program: AggregateProgram): Unit = {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            3, 3, 5,
            3, 5, 5,
            5, 5, 1
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("sink 0 and 6"){
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): Double = summarize(mid() == 6 || mid() == 0, _ + _, 1, 0)
        }

        def doTest(program: AggregateProgram): Unit = {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            6, 6, 6,
            6, 6, 6,
            2, 2, 1
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
    describe("should support average") {
      describe("average constant") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): Double = average(mid() == 0, 7)
        }

        def doTest(program: AggregateProgram): Unit = {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            7, 7, 7,
            7, 7, 7,
            7, 7, 7
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("average id") {
        trait ProgramMain extends AggregateProgram {
          self: ProgramImplDependencies =>
          override def main(): Double = average(mid() == 0, mid())
        }

        def doTest(program: AggregateProgram): Unit = {
          exec(program, ntimes = defaultNtimes)(net)

          assertNetworkValues((0 to 8).zip(List(
            3.5, 3.5, 3.5,
            3.5, 3.5, 3.5,
            3.5, 3.5, 8
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
