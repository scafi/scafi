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

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockG

  describe("BlockG") {
    describe("Should support distanceTo - hop") {
      def executeDistanceToSource(implicit net: Network with SimulatorOps): Network = {
        exec(new TestProgram {
          override def main(): Double = distanceTo(sense[Boolean]("source"), () => 1)
        }, ntimes = manyRounds)(net)
      }

      it("Should work if no source is specified") {
        executeDistanceToSource

        assertNetworkValues((0 to 8).zip(List(
          infinity, infinity, infinity,
          infinity, infinity, infinity,
          infinity, infinity, infinity
        )).toMap)(net)
      }
      it("Should work with multiple sources") {
        net.chgSensorValue("source", Set(0, 4), true)

        executeDistanceToSource

        assertNetworkValues((0 to 8).zip(List(
          0, 1, 1,
          1, 0, 1,
          1, 1, infinity
        )).toMap)(net)
      }
      it("Should react to a source change") {
        net.chgSensorValue("source", Set(0), true)

        executeDistanceToSource

        assertNetworkValues((0 to 8).zip(List(
          0, 1, 2,
          1, 1, 2,
          2, 2, infinity
        )).toMap)(net)

        net.chgSensorValue("source", Set(0), false)
        net.chgSensorValue("source", Set(4), true)

        executeDistanceToSource

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 0, 1,
          1, 1, infinity
        )).toMap)(net)
      }
    }
    describe("Should support broadcast - hop") {
      def executeBroadcastFromSource(implicit net: Network with SimulatorOps): Network = {
        exec(new TestProgram {
          override def main(): Double = broadcast(sense[Boolean]("source"), mid(), () => 1)
        }, ntimes = manyRounds)(net)
      }

      it("The source should broadcast its value") {
        net.chgSensorValue("source", Set(4), true)
        executeBroadcastFromSource

        assertNetworkValues((0 to 8).zip(List(
          4, 4, 4,
          4, 4, 4,
          4, 4, 8
        )).toMap)(net)
      }
      it("Should react to a source change") {
        net.chgSensorValue("source", Set(0), true)
        executeBroadcastFromSource

        net.chgSensorValue("source", Set(0), false)
        net.chgSensorValue("source", Set(7), true)
        executeBroadcastFromSource

        assertNetworkValues((0 to 8).zip(List(
          7, 7, 7,
          7, 7, 7,
          7, 7, 8
        )).toMap)(net)
      }
      it("Should work with multiple sources") {
        net.chgSensorValue("source", Set(0, 7), true)
        executeBroadcastFromSource

        //2 and 4 are equidistant from the sources -> they can assume both value
        assert(net.valueMap[Double]().forall {
          case (mid, r) if Set(2, 4) contains mid => r == 0.0 || r == 7.0
          case (mid, r) if Set(0, 1, 3) contains mid => r == 0.0
          case (mid, r) if Set(5, 6, 7) contains mid => r == 7.0
          case (8, 8.0) => true
          case _ => false
        })
      }
    }
    describe("Should support distanceBetween - hop") {
      def executeDistanceBetween(implicit net: Network with SimulatorOps): Network = {
        exec(new TestProgram {
          override def main(): Double = distanceBetween(sense("source"), sense("destination"), () => 1)
        }, ntimes = manyRounds)(net)
      }
      it("should work in a basic scenario (single source single destination") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(1), true)

        executeDistanceBetween

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 1, 1,
          1, 1, infinity
        )).toMap)(net)
      }
      it("the lowest distance should be broadcasted") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(1), true)
        net.chgSensorValue("destination", Set(7), true)

        executeDistanceBetween

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 1, 1,
          1, 1, infinity
        )).toMap)(net)
      }
      it("produce infinity if there is no path between source and destination") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(8), true)

        executeDistanceBetween

        assertNetworkValues((0 to 8).zip(List(
          infinity, infinity, infinity,
          infinity, infinity, infinity,
          infinity, infinity, 0
        )).toMap)(net)
      }
      it("should react to a distance change") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(1), true)

        executeDistanceBetween

        net.chgSensorValue("destination", Set(1), false)
        net.chgSensorValue("destination", Set(6), true)

        executeDistanceBetween

        assertNetworkValues((0 to 8).zip(List(
          2, 2, 2,
          2, 2, 2,
          2, 2, infinity
        )).toMap)(net)
      }
    }
    describe("Channel") {
      val smallChannelWidth: Double = 0.1
      def executeChannel(implicit net: Network with SimulatorOps): Network = {
        exec(new TestProgram {
          override def main(): Boolean = channel(sense("source"), sense("destination"), smallChannelWidth)
        }, ntimes = manyRounds)(net)
      }
      it("should mark as true the devices on the shortest path") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(2), true)

        executeChannel

        assertNetworkValues((0 to 8).zip(List(
          true, true, true,
          false, false, false,
          false, false, false
        )).toMap)(net)
      }
      it("should create multiple channels, if needed") {
        net.chgSensorValue("source", Set(0,6), true)
        net.chgSensorValue("destination", Set(2,7), true)

        executeChannel

        assertNetworkValues((0 to 8).zip(List(
          true, true, true,
          false, false, false,
          true, true, false
        )).toMap)(net)
      }
      it("every device should compute false if there is no channel") {
        executeChannel

        assertNetworkValues((0 to 8).zip(List(
          false, false, false,
          false, false, false,
          false, false, false
        )).toMap)(net)
      }
      it("should react to a change in the network") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(2), true)

        executeChannel

        net.chgSensorValue("destination", Set(2), false)
        net.chgSensorValue("destination", Set(6), true)

        executeChannel

        assertNetworkValues((0 to 8).zip(List(
          true, false, false,
          true, false, false,
          true, false, false
        )).toMap)(net)
      }
    }
    describe("G[V]") {
      def unitaryIncrement: Int => Int = _ + 1
      def executeG(implicit net: Network with SimulatorOps): Network = {
        exec(new TestProgram {
          override def main(): Int = G(sense("source"),  mux(sense("source")){0}{1}, unitaryIncrement, () => 1)
        }, ntimes = manyRounds)(net)
      }
      it("should accumulate over a gradient") {
        net.chgSensorValue("source", Set(0), true)

        executeG

        assertNetworkValues((0 to 8).zip(List(
          0, 1, 2,
          1, 1, 2,
          2, 2, 1
        )).toMap)(net)
      }
      it("should react to a source change") {
        net.chgSensorValue("source", Set(0), true)

        executeG

        net.chgSensorValue("source", Set(0), false)
        net.chgSensorValue("source", Set(7), true)

        executeG

        assertNetworkValues((0 to 8).zip(List(
          2, 2, 2,
          1, 1, 1,
          1, 0, 1
        )).toMap)(net)
      }
    }
  }
}
