package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._


class TestBlockG extends FunSpec with BeforeAndAfterEach {
  import ScafiAssertions._
  import ScafiTestUtils._

  implicit var net: Network with SimulatorOps = manhattanNet(detachedNodesCoords = Set((2,2)))
  val infinity: Double = Double.PositiveInfinity

  def restartNetwork(): Unit = {
    net = manhattanNet(detachedNodesCoords = Set((2,2)))
    net.addSensor(name = "source", value = false)
    net.addSensor(name = "destination", value = false)
  }
  override protected def beforeEach(): Unit = restartNetwork()

  private[this] trait TestProgramDefs {
    self: BlockGInterface =>
    def hopMetric: Metric
  }

  private[this] trait TestProgramStandard extends AggregateProgram with ScafiStandardLanguage
    with StandardSensors with ScafiStandardLibraries.BlockG with TestProgramDefs {
    override def hopMetric: Metric = () => constantRead(1)
  }
  private[this] trait TestProgramFC extends AggregateProgram with ScafiFCLanguage
    with StandardSensors with ScafiFCLibraries.BlockG with TestProgramDefs {
    override def hopMetric: Metric = () => constantRead(1)
  }

  private[this] type ProgramImplDependencies = BlockGInterface with ScafiBaseLanguage with TestProgramDefs

  def execProgram(program: AggregateProgram)(implicit net: Network with SimulatorOps): Network =
    exec(program, ntimes = manyRounds)(net)._1

  describe("BlockG") {
    describe("Should support distanceTo - hop") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Double = distanceTo(sense[Boolean]("source"), hopMetric)
      }

      describe("Should work if no source is specified") {
        def doTest(program: AggregateProgram) {
          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            infinity, infinity, infinity,
            infinity, infinity, infinity,
            infinity, infinity, infinity
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("Should work with multiple sources") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0, 4), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            0, 1, 1,
            1, 0, 1,
            1, 1, infinity
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("Should react to a source change") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            0, 1, 2,
            1, 1, 2,
            2, 2, infinity
          )).toMap)(net)

          net.chgSensorValue("source", Set(0), false)
          net.chgSensorValue("source", Set(4), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 0, 1,
            1, 1, infinity
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
    describe("Should support broadcast - hop") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Double = broadcast(sense[Boolean]("source"), mid(), hopMetric)
      }

      describe("The source should broadcast its value") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(4), true)
          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            4, 4, 4,
            4, 4, 4,
            4, 4, 8
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("Should react to a source change") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)
          execProgram(program)

          net.chgSensorValue("source", Set(0), false)
          net.chgSensorValue("source", Set(7), true)
          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            7, 7, 7,
            7, 7, 7,
            7, 7, 8
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("Should work with multiple sources") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0, 7), true)
          execProgram(program)

          //2 and 4 are equidistant from the sources -> they can assume both value
          assert(net.valueMap[Double]().forall {
            case (mid, r) if Set(2, 4) contains mid => r == 0.0 || r == 7.0
            case (mid, r) if Set(0, 1, 3) contains mid => r == 0.0
            case (mid, r) if Set(5, 6, 7) contains mid => r == 7.0
            case (8, 8.0) => true
            case _ => false
          })
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
    }
    describe("Should support distanceBetween - hop") {
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Double = distanceBetween(sense("source"), sense("destination"), hopMetric)
      }

      describe("should work in a basic scenario (single source single destination") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)
          net.chgSensorValue("destination", Set(1), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 1, 1,
            1, 1, infinity
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("the lowest distance should be broadcasted") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)
          net.chgSensorValue("destination", Set(1), true)
          net.chgSensorValue("destination", Set(7), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            1, 1, 1,
            1, 1, 1,
            1, 1, infinity
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("produce infinity if there is no path between source and destination") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)
          net.chgSensorValue("destination", Set(8), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            infinity, infinity, infinity,
            infinity, infinity, infinity,
            infinity, infinity, 0
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("should react to a distance change") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)
          net.chgSensorValue("destination", Set(1), true)

          execProgram(program)

          net.chgSensorValue("destination", Set(1), false)
          net.chgSensorValue("destination", Set(6), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            2, 2, 2,
            2, 2, 2,
            2, 2, infinity
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
    describe("Channel") {
      val smallChannelWidth: Double = 0.1
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Boolean = channel(sense("source"), sense("destination"), smallChannelWidth)
      }

      describe("should mark as true the devices on the shortest path") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)
          net.chgSensorValue("destination", Set(2), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            true, true, true,
            false, false, false,
            false, false, false
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("should create multiple channels, if needed") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0, 6), true)
          net.chgSensorValue("destination", Set(2, 7), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            true, true, true,
            false, false, false,
            true, true, false
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("every device should compute false if there is no channel") {
        def doTest(program: AggregateProgram) {
          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            false, false, false,
            false, false, false,
            false, false, false
          )).toMap)(net)
        }

        it(inStandard) {
          doTest(new TestProgramStandard with ProgramMain)
        }
        it(inFC) {
          doTest(new TestProgramFC with ProgramMain)
        }
      }
      describe("should react to a change in the network") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)
          net.chgSensorValue("destination", Set(2), true)

          execProgram(program)

          net.chgSensorValue("destination", Set(2), false)
          net.chgSensorValue("destination", Set(6), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            true, false, false,
            true, false, false,
            true, false, false
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
    describe("G[V]") {
      def unitaryIncrement: Int => Int = _ + 1
      trait ProgramMain extends AggregateProgram {
        self: ProgramImplDependencies =>
        override def main(): Int = G(sense("source"),  mux(sense("source")){0}{1}, unitaryIncrement, hopMetric)
      }
      describe("should accumulate over a gradient") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            0, 1, 2,
            1, 1, 2,
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
      describe("should react to a source change") {
        def doTest(program: AggregateProgram) {
          net.chgSensorValue("source", Set(0), true)

          execProgram(program)

          net.chgSensorValue("source", Set(0), false)
          net.chgSensorValue("source", Set(7), true)

          execProgram(program)

          assertNetworkValues((0 to 8).zip(List(
            2, 2, 2,
            1, 1, 1,
            1, 0, 1
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
